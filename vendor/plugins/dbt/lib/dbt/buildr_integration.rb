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

class Dbt #nodoc
  class Buildr
    def self.add_idea_data_sources_from_configuration_file(buildr_project = nil)
      return unless load_dbt_configuration_data

      Dbt.repository.configuration_keys.each do |config_key|
        database_key = nil
        environment_key = nil
        Dbt.repository.database_keys.each do |key|
          if config_key =~ /^(#{key})_(.*)/ && (database_key.nil? || key.length > database_key.length)
            database_key = key
            environment_key = $2
          end
        end
        add_idea_data_source(database_key, environment_key, buildr_project) if database_key
      end
    end

    def self.add_idea_data_source(database_key, environment_key, buildr_project = nil)
      unless load_dbt_configuration_data
        info("Skipping addition of data source #{database_key} to idea due to missing configuration file.")
        return
      end

      config =
        begin
          Dbt.configuration_for_key(database_key, environment_key)
        rescue => e
          info("Missing configuration #{database_key} in environment #{environment_key}, " +
                 "skipping addition of data source #{database_key} to idea. Cause: #{e}")
          return
        end

      buildr_project = get_buildr_project(buildr_project)
      add_idea_data_source_from_config(buildr_project, config)
    end

    private

    def self.get_buildr_project(buildr_project)
      if buildr_project.nil? && ::Buildr.application.current_scope.size > 0
        buildr_project = ::Buildr.project(::Buildr.application.current_scope.join(':')) rescue nil
      end
      buildr_project
    end

    def self.add_idea_data_source_from_config(buildr_project, config)
      name = config.key
      jdbc_url = config.build_jdbc_url
      username = config.username
      password = config.password

      if config.is_a?(Dbt::MssqlDbConfig) || config.is_a?(Dbt::TinyTdsDbConfig)
        buildr_project.ipr.add_sql_server_data_source(name, :url => jdbc_url, :username => username, :password => password)
      else
        buildr_project.ipr.add_postgres_data_source(name, :url => jdbc_url, :username => username, :password => password)
      end
    end

    def self.load_dbt_configuration_data
      unless Dbt.repository.is_configuration_data_loaded?
        begin
          Dbt.repository.load_configuration_data
        rescue Exception => e
          info("Unable to load database configuration from #{Dbt::Config.config_filename}. Cause: #{e}")
          return false
        end
      end
      return true
    end
  end
end
