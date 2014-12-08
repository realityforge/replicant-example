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
    module Audit
      TEMPLATE_DIRECTORY = "#{File.dirname(__FILE__)}/templates"
      FACETS = [:audit]
    end
  end
end

Domgen.template_set(:audit_psql) do |template_set|
  template_set.template(Domgen::Generator::Audit::FACETS + [:pgsql],
                        :entity,
                        "#{Domgen::Generator::Audit::TEMPLATE_DIRECTORY}/psql_view.sql.erb",
                        '#{entity.data_module.name}/views/#{entity.data_module.sql.schema}.vw#{entity.name}.sql')
end

Domgen.template_set(:audit_mssql) do |template_set|
  template_set.template(Domgen::Generator::Audit::FACETS + [:mssql],
                        :entity,
                        "#{Domgen::Generator::Audit::TEMPLATE_DIRECTORY}/mssql_view.sql.erb",
                        '#{entity.data_module.name}/views/#{entity.data_module.sql.schema}.vw#{entity.name}.sql')
  template_set.template(Domgen::Generator::Audit::FACETS + [:mssql],
                        :entity,
                        "#{Domgen::Generator::Audit::TEMPLATE_DIRECTORY}/mssql_finalize.sql.erb",
                        '#{entity.data_module.name}/finalize/#{entity.data_module.sql.schema}.vw#{entity.name}_finalize.sql')
  template_set.template(Domgen::Generator::Audit::FACETS + [:mssql],
                        :entity,
                        "#{Domgen::Generator::Audit::TEMPLATE_DIRECTORY}/mssql_triggers.sql.erb",
                        '#{entity.data_module.name}/triggers/#{entity.data_module.sql.schema}.vw#{entity.name}_triggers.sql')
end
