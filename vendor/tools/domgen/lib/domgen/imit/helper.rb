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
  module Imit
    module Helper

      def query_getter(a)
        a.remote_reference? ? "get#{a.referencing_link_name}()" : getter_for(a)
      end

      def process_parameter(entity, parameter_name, javaql, prefix)
        if entity.attribute_by_name?(parameter_name)
          a = entity.attribute_by_name(parameter_name)
          value = Reality::Naming.camelize(parameter_name)
          if a.remote_reference?
            value = "#{value}.#{getter_for(a.referenced_remote_entity.primary_key)}"
            value = " null == #{Reality::Naming.camelize(parameter_name)} ? null : #{value}" if a.nullable?
          end
          return "#{prefix} java.util.Objects.equals( e.#{query_getter(a)}, #{value} ) #{javaql}"
        else
          # Handle parameters that are the primary keys of related entities
          entity.attributes.select { |a| a.reference? && a.referencing_link_name == parameter_name }.each do |a|
            return "#{prefix} java.util.Objects.equals( e.get#{a.name}().#{getter_for(a.referenced_entity.primary_key)}, #{Reality::Naming.camelize(parameter_name)} ) #{javaql}"
          end
          entity.attributes.select { |a| a.remote_reference? && a.referencing_link_name == parameter_name }.each do |a|
            return "#{prefix} java.util.Objects.equals( e.get#{a.referencing_link_name}(), #{Reality::Naming.camelize(parameter_name)} ) #{javaql}"
          end
          return nil
        end
      end

      def lambda_query(query)
        javaql = ''
        query_text = nil
        query_text = $1 if query.name =~ /^[fF]indAllBy(.+)$/
        query_text = $1 if query.name =~ /^[fF]indBy(.+)$/
        query_text = $1 if query.name =~ /^[gG]etBy(.+)$/
        query_text = $1 if query.name =~ /^[cC]ountBy(.+)$/
        raise "Unable to derive lambda based query for #{query.qualified_name}" unless query_text

        entity = query.dao.entity

        while true
          if query_text =~ /(.+)(And|Or)([A-Z].*)/
            parameter_name = $3
            operation = $2 == 'And' ? '&&' : '||'
            query_text = $1
            javaql = process_parameter(entity, parameter_name, javaql, operation)
            break if javaql.nil?
          else
            parameter_name = query_text
            javaql = process_parameter(entity, parameter_name, javaql, nil)
            break
          end
        end
        raise "Unable to derive query #{query.qualified_name}" unless javaql
        javaql
      end

      def query_component_result_type(query)
        query.result_entity? ?
          query.entity.imit.qualified_name :
          query.result_struct? ?
            query.struct.gwt.qualified_name :
            Domgen::TypeDB.characteristic_type_by_name(query.result_type).java.object_type
      end

      def query_result_type(query)
        return 'int' if query.query_type != :select
        name = query_component_result_type(query)
        return "#{nullability_annotation(false)} java.util.List<#{name}>" if query.multiplicity == :many
        "#{nullability_annotation(query.multiplicity == :zero_or_one)} #{name}"
      end

    end
  end
end
