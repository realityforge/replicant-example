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
  module JAXB
    class JaxbStructField < Domgen.ParentedElement(:field)
    end

    class JaxbStruct < Domgen.ParentedElement(:struct)
    end

    class JaxbEnumeration < Domgen.ParentedElement(:enumeration)
    end

    class JaxbDataModule < Domgen.ParentedElement(:data_module)
    end

    class JaxbPackage < Domgen.ParentedElement(:repository)
      include Domgen::Java::BaseJavaGenerator

      java_artifact :marshalling_test, :data_type, :server, :ee, '#{repository.name}JaxbMarshallingTest'
    end
  end

  FacetManager.define_facet(:jaxb,
                            {
                              Struct => Domgen::JAXB::JaxbStruct,
                              StructField => Domgen::JAXB::JaxbStructField,
                              EnumerationSet => Domgen::JAXB::JaxbEnumeration,
                              DataModule => Domgen::JAXB::JaxbDataModule,
                              Repository => Domgen::JAXB::JaxbPackage
                            },
                            [:xml, :ee])
end
