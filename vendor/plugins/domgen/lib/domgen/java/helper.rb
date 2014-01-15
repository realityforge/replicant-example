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
  module Java
    module Helper
      def nullability_annotation(is_nullable)
        is_nullable ? "@javax.annotation.Nullable" : "@javax.annotation.Nonnull"
      end

      def supports_nullable?(extension, modality = :default)
        !(extension.primitive?(modality) || extension.java_type(modality).to_s == 'void')
      end

      def annotated_type(characteristic, characteristic_key, modality = :default)
        extension = characteristic.send(characteristic_key)
        unless supports_nullable?(extension, modality)
          return extension.java_type(modality)
        else
          return "#{nullability_annotation(characteristic.nullable?)} #{extension.java_type(modality)}"
        end
      end

      def javabean_property_name(key)
        name = key.to_s
        return name if name == name.upcase
        "#{name[0,1].downcase}#{name[1,name.length]}"
      end

      def getter_prefix(attribute)
        attribute.boolean? ? "is" : "get"
      end

      def description_javadoc_for(element, depth = "  ")
        description = element.tags[:Description]
        return '' unless description
        return <<JAVADOC
#{depth}/**
#{depth} * #{description.gsub(/\n+\Z/,"").gsub("\n\n","\n<br />\n").gsub("\n","\n#{depth} * ")}
#{depth} */
JAVADOC
      end

      def modality_default_to_transport(variable_name, characteristic, characteristic_key)
        extension = characteristic.send(characteristic_key)

        return variable_name if extension.java_type == extension.java_type(:boundary)

        transform = variable_name
        if characteristic.characteristic_type_key == :enumeration
          if characteristic.enumeration.numeric_values?
            transform = "#{variable_name}.ordinal()"
          else
            transform = "#{variable_name}.name()"
          end
        elsif characteristic.characteristic_type_key == :reference
          transform = "#{variable_name}.get#{characteristic.referenced_entity.primary_key.name}()"
        end
        if characteristic.nullable?
          transform = "null == #{variable_name} ? null : #{transform}"
        end
        transform
      end

      def modality_boundary_to_default(variable_name, characteristic, characteristic_key)
        extension = characteristic.send(characteristic_key)

        return variable_name if extension.java_type == extension.java_type(:boundary)
        return "$#{variable_name}" if characteristic.reference? && characteristic.collection?

        transform = variable_name
        if characteristic.characteristic_type_key == :reference
          transform = "_#{characteristic.referenced_entity.qualified_name.gsub('.','')}DAO.getBy#{characteristic.referenced_entity.primary_key.name}( #{variable_name} )"
        end
        if characteristic.nullable? && transform != variable_name
          transform = "(null == #{variable_name} ? null : #{transform})"
        end
        transform
      end
    end
  end
end
