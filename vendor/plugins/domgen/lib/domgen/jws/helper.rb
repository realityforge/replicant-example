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
  module JWS
    module Helper

      def xsd_type(characteristic)
        return ' type="xs:dateTime"' if characteristic.datetime?
        return ' type="xs:date"' if characteristic.date?
        return ' type="xs:boolean"' if characteristic.boolean?
        return ' type="xs:integer"' if characteristic.integer?
        return ' type="xs:string"' if characteristic.text?
        return xsd_type(characteristic.referenced_entity.primary_key) if characteristic.reference?
        return " type=\"tns:#{characteristic.referenced_struct.name}\"" if characteristic.struct?
        return " type=\"tns:#{characteristic.enumeration.name}\"" if characteristic.enumeration?
        raise "unknown type #{characteristic.characteristic_type} and can not convert to xsd"
      end

      def jws_element_occurrences(characteristic)
        s = ''
        s << ' minOccurs="0"' if (characteristic.nullable? || characteristic.collection?)
        s << ' maxOccurs="unbounded"' if characteristic.collection?
        s
      end

      def xsd_attribute_use(characteristic)
        characteristic.nullable? ? ' use="optional"' : ' use="required"'
      end
    end
  end
end
