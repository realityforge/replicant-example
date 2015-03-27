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
  module Generator
    module Sync
      TEMPLATE_DIRECTORY = "#{File.dirname(__FILE__)}/templates"
      HELPERS = [Domgen::Java::Helper, Domgen::JPA::Helper]
      FACETS = [:sql, :sync]
    end
  end
end
Domgen.template_set(:sync_ejb) do |template_set|
  template_set.template(Domgen::Generator::Sync::FACETS,
                        :data_module,
                        "#{Domgen::Generator::Sync::TEMPLATE_DIRECTORY}/abstract_master_sync_ejb.java.erb",
                        'main/java/#{data_module.sync.qualified_abstract_master_sync_ejb_name.gsub(".","/")}.java',
                        Domgen::Generator::Sync::HELPERS,
                        :guard => 'data_module.sync.master_data_module?')
  template_set.template(Domgen::Generator::Sync::FACETS,
                        :data_module,
                        "#{Domgen::Generator::Sync::TEMPLATE_DIRECTORY}/sync_ejb.java.erb",
                        'main/java/#{data_module.sync.qualified_sync_ejb_name.gsub(".","/")}.java',
                        Domgen::Generator::Sync::HELPERS,
                        :guard => 'data_module.sync.master_data_module?')
  template_set.template(Domgen::Generator::Sync::FACETS,
                        :data_module,
                        "#{Domgen::Generator::Sync::TEMPLATE_DIRECTORY}/sync_context_impl.java.erb",
                        'main/java/#{data_module.sync.qualified_sync_context_impl_name.gsub(".","/")}.java',
                        Domgen::Generator::Sync::HELPERS,
                        :guard => 'data_module.sync.master_data_module?')
end
Domgen.template_set(:sync_master_ejb) do |template_set|
  template_set.template(Domgen::Generator::Sync::FACETS,
                        :data_module,
                        "#{Domgen::Generator::Sync::TEMPLATE_DIRECTORY}/abstract_master_sync_ejb.java.erb",
                        'main/java/#{data_module.sync.qualified_abstract_master_sync_ejb_name.gsub(".","/")}.java',
                        Domgen::Generator::Sync::HELPERS,
                        :guard => 'data_module.sync.master_data_module?')
end
