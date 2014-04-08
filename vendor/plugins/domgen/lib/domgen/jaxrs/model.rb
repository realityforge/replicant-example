#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

module Domgen
  module JaxRS
    module MediaTypeEnabled
      attr_reader :consumes

      def consumes=(consumes)
        consumes = [consumes] unless consumes.is_a?(Array)
        consumes.each do |media_type|
          raise "Specified media type '#{media_type}' is not valid" unless valid_media_type?(media_type)
        end
        @consumes = consumes
      end

      attr_reader :produces

      def produces=(produces)
        produces = [produces] unless produces.is_a?(Array)
        produces.each do |media_type|
          raise "Specified media type '#{media_type}' is not valid" unless valid_media_type?(media_type)
        end
        @produces = produces
      end

      def valid_media_type?(media_type)
        [:json, :xml, :plain].include?(media_type)
      end
    end

    class JaxRsClass < Domgen.ParentedElement(:service, <<-INIT)
      @produces = [:json, :xml]
      @consumes = [:json, :xml]
      INIT
      include MediaTypeEnabled
      include Domgen::Java::BaseJavaGenerator

      def short_service_name
        service.name.to_s =~ /^(.*)Service/ ? service.name.to_s[0..-7] : service.name
      end

      java_artifact :service, :service, :server, :ee, '#{short_service_name}RestService'
      java_artifact :boundary, :service, :server, :ee, '#{service_name}Impl'

      attr_accessor :boundary_extends

      attr_writer :path

      def path
        return @path unless @path.nil?
        return "/#{Domgen::Naming.underscore(short_service_name)}"
      end
    end

    class JaxRsParameter < Domgen.ParentedElement(:parameter)

      include Domgen::Java::EEJavaCharacteristic

      attr_writer :param_key

      def param_key
        @param_key || Domgen::Naming.camelize(characteristic.name)
      end

      attr_accessor :default_value

      def param_type
        @param_type || :query
      end

      def param_type=(param_type)
        raise "Unknown param_type #{param_type}" unless valid_param_type?(param_type)
        @param_type = param_type
      end

      protected

      def valid_param_type?(param_type)
        [:query, :cookie, :path, :form, :header].include?(param_type)
      end

      def characteristic
        parameter
      end
    end

    class JaxRsMethod < Domgen.ParentedElement(:method)

      include MediaTypeEnabled

      attr_writer :path

      def path
        if @path
          return @path == '' ? nil : @path
        end
        base_path = "/#{Domgen::Naming.underscore(method.name)}"
        path_parameters = method.parameters.select { |p| p.jaxrs? && :path == p.jaxrs.param_type }
        return base_path if path_parameters.empty?
        return "#{base_path}/{#{path_parameters.collect { |p| p.jaxrs.param_key }.join("/")}}"
      end

      def http_method=(http_method)
        raise "Specified http method '#{http_method}' is not valid" unless valid_http_method?(http_method)
        @http_method = http_method
      end

      def http_method
        return @http_method if @http_method
        name = method.name.to_s
        if name =~ /^Get[A-Z].*/ || name =~ /^Find[A-Z].*/
          return "GET"
        elsif name =~ /^Delete[A-Z].*/ || name =~ /^Remove[A-Z].*/
          return "DELETE"
        else
          return "POST"
        end
      end

      def valid_http_method?(http_method)
        %(GET DELETE PUT POST HEAD OPTIONS).include?(http_method)
      end
    end

    class JaxRsReturn < Domgen.ParentedElement(:result)
      include Domgen::Java::EEJavaCharacteristic

      protected

      def characteristic
        result
      end
    end

    class JaxRsException < Domgen.ParentedElement(:exception)
    end

    class JaxRsPackage < Domgen.ParentedElement(:data_module)
    end

    class JaxRsApplication < Domgen.ParentedElement(:repository)
      include Domgen::Java::BaseJavaGenerator

      attr_writer :path

      def path
        @path || 'api'
      end

      java_artifact :abstract_application, :service, :server, :ee, '#{repository.name}JaxRsApplication'
    end
  end

  FacetManager.define_facet(:jaxrs,
                            {
                              Service => Domgen::JaxRS::JaxRsClass,
                              Method => Domgen::JaxRS::JaxRsMethod,
                              Parameter => Domgen::JaxRS::JaxRsParameter,
                              Exception => Domgen::JaxRS::JaxRsException,
                              Result => Domgen::JaxRS::JaxRsReturn,
                              DataModule => Domgen::JaxRS::JaxRsPackage,
                              Repository => Domgen::JaxRS::JaxRsApplication
                            },
                            [:ee])
end
