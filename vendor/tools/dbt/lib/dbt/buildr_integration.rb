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

      valid_environments = %w(development uat training production staging test ci import)

      Dbt.repository.configuration_keys.each do |config_key|
        database_key = nil
        environment_key = nil
        Dbt.repository.database_keys.each do |key|
          pattern = Dbt::Config.default_database?(key) ? /([^_]+)/ : /^#{key}_(.*)/
          if config_key =~ pattern
            matched_valued = $1
            if (database_key.nil? || key.length > database_key.length) && valid_environments.include?(matched_valued)
              database_key = key
              environment_key = matched_valued
            end
          end
        end
        add_idea_data_source(database_key, environment_key, buildr_project) if database_key
      end
    end

    def self.add_idea_data_source(database_key, environment_key, buildr_project = nil)
      unless load_dbt_configuration_data
        info("Skipping addition of data source '#{database_key}' to idea as unable load database configuration file '#{Dbt::Config.config_filename}'.")
        return
      end

      config =
        begin
          Dbt.configuration_for_key(database_key, environment_key)
        rescue Exception
          info(<<MSG)
Skipping addition of data source '#{database_key}' (in environment '#{environment_key}') to idea due to database configuration missing from configuration file #{Dbt::Config.config_filename}.
MSG
          return
        end

      add_idea_data_source_from_config(config, buildr_project)
    end

    def self.add_idea_data_source_from_config_key(config_key, buildr_project = nil)
      unless load_dbt_configuration_data
        info("Skipping addition of data source for config key '#{config_key}' to idea as unable load database configuration file '#{Dbt::Config.config_filename}'.")
        return
      end

      config =
        begin
          Dbt.repository.configuration_for_key(config_key)
        rescue Exception
          info(<<MSG)
Skipping addition of data source for config key '#{config_key}' to idea due to database configuration missing from configuration file #{Dbt::Config.config_filename}.
MSG
          return
        end

      add_idea_data_source_from_config(config, buildr_project)
    end

    private

    def self.get_buildr_project(buildr_project)
      if buildr_project.nil? && ::Buildr.application.current_scope.size > 0
        buildr_project = ::Buildr.project(::Buildr.application.current_scope.join(':')) rescue nil
      end
      buildr_project
    end

    def self.add_idea_data_source_from_config(config, buildr_project = nil)
      name = config.key
      jdbc_url = config.build_jdbc_url
      username = config.username
      password = config.password

      buildr_project = get_buildr_project(buildr_project)

      if config.is_a?(Dbt::MssqlDbConfig) || config.is_a?(Dbt::TinyTdsDbConfig)
        buildr_project.ipr.add_sql_server_data_source(name, :url => jdbc_url, :username => username, :password => password)
      else
        buildr_project.ipr.add_postgres_data_source(name, :url => jdbc_url, :username => username, :password => password)
      end
    end

    def self.load_dbt_configuration_data
      file(File.expand_path(Dbt::Config.config_filename)).invoke

      Dbt.repository.load_configuration_data
    end
  end
end
