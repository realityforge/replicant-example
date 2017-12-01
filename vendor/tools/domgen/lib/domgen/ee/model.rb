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
  FacetManager.facet(:ee => [:application, :java]) do |facet|
    facet.suggested_facets << :redfish

    facet.enhance(Repository) do
      include Domgen::Java::BaseJavaGenerator
      include Domgen::Java::JavaClientServerApplication

      def integration_test_modules
        integration_test_modules_map.dup
      end

      def add_integration_test_module(name, classname)
        Domgen.error("Attempting to define duplicate integration test module for ejb facet. Name = '#{name}', Classname = '#{classname}'") if integration_test_modules_map[name.to_s]
        integration_test_modules_map[name.to_s] = classname
      end

      def version
        @version || '7'
      end

      attr_writer :web_metadata_complete

      def web_metadata_complete?
        @web_metadata_complete.nil? ? false : @web_metadata_complete
      end

      def web_xml_content_fragments
        @web_xml_content_fragments ||= []
      end

      def web_xml_fragments
        @web_xml_fragments ||= []
      end

      def resolved_web_xml_fragments
        self.web_xml_fragments.collect do |fragment|
          repository.read_file(fragment)
        end
      end

      def cdi_scan_excludes
        @cdi_scan_excludes ||= []
      end

      # A beans.xml is created in both the model and server components
      ['', 'model_'].each do |prefix|
        class_eval <<-RUBY
          def #{prefix}bean_discovery_mode=(mode)
            Domgen.error("Unknown \#{prefix}bean discovery mode '\#{mode}'") unless %w(all annotated none).include?(mode)
            @#{prefix}bean_discovery_mode = mode
          end

          def #{prefix}bean_discovery_mode
            @#{prefix}bean_discovery_mode ||= 'annotated'
          end

          def #{prefix}beans_xml_content_fragments
            @#{prefix}beans_xml_content_fragments ||= []
          end

          def #{prefix}beans_xml_fragments
            @#{prefix}beans_xml_fragments ||= []
          end

          def resolved_#{prefix}beans_xml_fragments
            self.#{prefix}beans_xml_fragments.collect do |fragment|
              repository.read_file(fragment)
            end
          end
        RUBY
      end

      attr_writer :server_event_package

      def server_event_package
        @server_event_package || "#{server_package}.event"
      end

      def version=(version)
        Domgen.error("Unknown version '#{version}'") unless %w(6 7).include?(version)
        @version = version
      end

      java_artifact :cdi_qualifier, nil, :shared, :ee, '#{repository.name}'
      java_artifact :cdi_qualifier_literal, nil, :shared, :ee, '#{repository.name}Literal'
      java_artifact :abstract_filter, :filter, :server, :ee, 'Abstract#{repository.name}Filter'
      java_artifact :abstract_app_server, :test, :integration, :ee, 'Abstract#{repository.name}AppServer', :sub_package => 'util'
      java_artifact :abstract_provisioner, :test, :integration, :ee, 'Abstract#{repository.name}Provisioner', :sub_package => 'util'
      java_artifact :provisioner, :test, :integration, :ee, '#{repository.name}Provisioner', :sub_package => 'util'
      java_artifact :app_server, :test, :integration, :ee, '#{repository.name}AppServer', :sub_package => 'util'
      java_artifact :app_server_factory, :test, :integration, :ee, '#{repository.name}AppServerFactory', :sub_package => 'util'
      java_artifact :abstract_integration_test, :test, :integration, :ee, 'Abstract#{repository.name}GlassFishTest', :sub_package => 'util'
      java_artifact :base_integration_test, :test, :integration, :ee, '#{repository.name}GlassFishTest', :sub_package => 'util'
      java_artifact :deploy_test, nil, :integration, :ee, '#{repository.name}DeployTest'
      java_artifact :aggregate_integration_test, :test, :integration, :ee, '#{repository.name}AggregateIntegrationTest', :sub_package => 'util'
      java_artifact :message_module, :test, :server, :ee, '#{repository.name}MessagesModule', :sub_package => 'util'

      attr_writer :custom_app_server

      def custom_app_server?
        @custom_app_server.nil? ? false : !!@custom_app_server
      end

      attr_writer :custom_provisioner

      def custom_provisioner?
        @custom_provisioner.nil? ? (repository.redfish? ? repository.redfish.custom_configuration? : false) : !!@custom_provisioner
      end

      attr_writer :custom_base_integration_test

      def custom_base_integration_test?
        @custom_base_integration_test.nil? ? false : !!@custom_base_integration_test
      end

      protected

      def integration_test_modules_map
        @integration_test_modules_map ||= {}
      end
    end

    facet.enhance(DataModule) do
      include Domgen::Java::EEClientServerJavaPackage

      attr_writer :server_event_package

      def server_event_package
        @server_event_package || resolve_package(:server_event_package)
      end

    end

    facet.enhance(Message) do
      include Domgen::Java::BaseJavaGenerator

      def name
        "#{message.name}"
      end

      def qualified_name
        "#{message.data_module.ee.server_event_package}.#{name}"
      end

      attr_writer :generate_test_literal

      def generate_test_literal?
        @generate_test_literal.nil? ? true : !!@generate_test_literal
      end

      java_artifact :message_literal, :test, :server, :ee, '#{message.name}TypeLiteral', :sub_package => 'util'
    end

    facet.enhance(MessageParameter) do
      include Domgen::Java::EEJavaCharacteristic

      def name
        parameter.name
      end

      protected

      def characteristic
        parameter
      end
    end

    facet.enhance(EnumerationSet) do
      def name
        "#{enumeration.name}"
      end

      def qualified_name
        "#{enumeration.data_module.ee.server_data_type_package}.#{name}"
      end
    end

    facet.enhance(Struct) do
      attr_writer :name

      def name
        @name || struct.name
      end

      def qualified_name
        "#{struct.data_module.ee.server_data_type_package}.#{self.name}"
      end
    end

    facet.enhance(StructField) do
      include Domgen::Java::EEJavaCharacteristic

      def name
        field.name
      end

      protected

      def characteristic
        field
      end
    end

    facet.enhance(Exception) do
      def name
        exception.name.to_s =~ /Exception$/ ? exception.name.to_s : "#{exception.name}Exception"
      end

      def qualified_name
        "#{exception.data_module.ee.server_service_package}.#{name}"
      end
    end

    facet.enhance(ExceptionParameter) do
      include Domgen::Java::EEJavaCharacteristic

      def name
        parameter.name
      end

      protected

      def characteristic
        parameter
      end
    end
  end
end
