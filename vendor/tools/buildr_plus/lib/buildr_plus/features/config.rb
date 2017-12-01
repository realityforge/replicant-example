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

BuildrPlus::FeatureManager.feature(:config) do |f|
  f.enhance(:Config) do
    attr_writer :application_config_location

    def application_config_location
      base_directory = File.dirname(Buildr.application.buildfile.to_s)
      "#{base_directory}/config/application.yml"
    end

    def application_config_example_location
      application_config_location.gsub(/\.yml$/, '.example.yml')
    end

    def application_config
      @application_config ||= load_application_config
    end

    attr_writer :environment

    def environment
      @environment || ENV['BUILD_ENV'] || 'development'
    end

    def environment_config
      raise "Attempting to configuration for #{self.environment} environment which is not present." unless self.application_config.environment_by_key?(self.environment)
      self.application_config.environment_by_key(self.environment)
    end

    def domain_environment_var(domain, key, default_value = nil)
      domain_name = Reality::Naming.uppercase_constantize(domain.name)
      scope = self.app_scope
      code = self.env_code

      return ENV["#{domain_name}_#{scope}_#{key}_#{code}"] if scope && ENV["#{domain_name}_#{scope}_#{key}_#{code}"]
      return ENV["#{domain_name}_#{key}_#{code}"] if ENV["#{domain_name}_#{key}_#{code}"]
      return ENV["#{scope}_#{key}_#{code}"] if scope && ENV["#{scope}_#{key}_#{code}"]

      ENV["#{scope}_#{key}_#{code}"] ||
        ENV["#{key}_#{code}"] ||
        ENV[key] ||
        default_value
    end

    def env(key, default_value = nil)
      environment_var(key, default_value) || BuildrPlus.error("Unable to locate expected environment variable #{key}")
    end

    def environment_var(key, default_value = nil)
      scope = self.app_scope
      code = self.env_code

      return ENV["#{scope}_#{key}_#{code}"] if scope && ENV["#{scope}_#{key}_#{code}"]

      ENV["#{key}_#{code}"] ||
        ENV[key] ||
        default_value
    end

    def user
      ENV['USER']
    end

    def app_scope
      return ENV['APP_SCOPE'] if ENV['APP_SCOPE']
      return ENV['PRODUCT_VERSION'] if running_in_jenkins?
      nil
    end

    def running_in_jenkins?
      !!ENV['JOB_NAME']
    end

    def env_code(environment = self.environment)
      if environment == 'development'
        'DEV'
      elsif environment == 'test'
        'TEST'
      elsif environment == 'uat'
        'UAT'
      elsif environment == 'training'
        'TRN'
      elsif environment == 'ci'
        'CI'
      elsif environment == 'production'
        'PRD'
      else
        environment
      end
    end

    def db_scope
      if running_in_jenkins?
        "#{self.app_scope}_"
      else
        "#{user || 'NOBODY'}#{self.app_scope.nil? ? '' : "_#{self.app_scope}"}_"
      end
    end

    def load_application_config!
      @application_config = load_application_config unless @application_config
    end

    def reload_application_config!
      @application_config = nil
      load_application_config!
    end

    def get_buildr_project
      if ::Buildr.application.current_scope.size > 0
        ::Buildr.project(::Buildr.application.current_scope.join(':')) rescue nil
      else
        Buildr.projects[0]
      end
    end

    def output_aux_confgs!(config = application_config)
      emit_database_yml(config)
    end

    private

    def load_application_config
      if !File.exist?(self.application_config_location) && File.exist?(self.application_config_example_location)
        FileUtils.cp self.application_config_example_location, self.application_config_location
      end
      data = File.exist?(self.application_config_location) ?
        YAML::load(ERB.new(IO.read(self.application_config_location)).result) :
        {}

      config = BuildrPlus::Config::ApplicationConfig.new(data)

      populate_configuration(config)

      output_aux_confgs!(config)

      config
    end

    def emit_database_yml(config)
      if BuildrPlus::FeatureManager.activated?(:dbt)
        ::Dbt.repository.configuration_data = config.to_database_yml

        ::SSRS::Config.config_data = config.to_database_yml if BuildrPlus::FeatureManager.activated?(:rptman)

        # Also need to write file out for processes that pick up database.yml (like packaged
        # database definitions run via java -jar)
        FileUtils.mkdir_p File.dirname(::Dbt::Config.config_filename)
        File.open(::Dbt::Config.config_filename, 'wb') do |file|
          file.write "# DO NOT EDIT: File is auto-generated\n"
          file.write config.to_database_yml(:jruby => true).to_yaml
        end
      end
    end

    def emit_rails_configuration(domain)
      properties = BuildrPlus::Redfish.build_property_set(domain, BuildrPlus::Config.environment_config)

      base_directory = File.dirname(Buildr.application.buildfile.to_s)

      FileUtils.mkdir_p "#{base_directory}/config"
      File.open("#{base_directory}/config/config.properties", 'wb') do |file|
        file.write "# DO NOT EDIT: File is auto-generated\n"
        data = ''
        domain.data['custom_resources'].each_pair do |key, resource_config|
          value = resource_config['properties']['value']
          data += "#{key.gsub('/', '.')}=#{value}\n"
        end

        properties.each_pair do |key, value|
          data.gsub!("${#{key}}", value)
        end
        data = Redfish::Interpreter::Interpolater.interpolate_definition(domain, :data => data)[:data]
        file.write data
      end
    end

    def populate_configuration(config)
      %w(development test).each do |environment_key|
        config.environment(environment_key) unless config.environment_by_key?(environment_key)
        populate_environment_configuration(config.environment_by_key(environment_key), false)
      end
      copy_development_settings(config.environment_by_key('development'), config.environment_by_key('test'))
      config.environments.each do |environment|
        unless %w(development test).include?(environment.key.to_s)
          populate_environment_configuration(environment, true)
        end
      end
    end

    def copy_development_settings(from, to)
      from.settings.each_pair do |key, value|
        to.setting(key, value) unless to.setting?(key)
      end
    end

    def populate_environment_configuration(environment, check_only = false)
      populate_database_configuration(environment, check_only)
      populate_broker_configuration(environment, check_only)
      populate_keycloak_configuration(environment, check_only)
      populate_ssrs_configuration(environment, check_only)
      unless check_only
        populate_volume_configuration(environment)
        populate_settings(environment)
      end
    end

    def populate_settings(environment)
      if BuildrPlus::FeatureManager.activated?(:keycloak)
        BuildrPlus::Keycloak.clients.each do |client|
          populate_keycloak_client_settings(environment, client)
        end
        BuildrPlus::Keycloak.remote_clients.each do |remote_client|
          populate_keycloak_remote_client_settings(environment, remote_client)
        end
      end
      if BuildrPlus::FeatureManager.activated?(:remote_references)
        BuildrPlus::RemoteReferences.remote_datasources.each do |remote_datasource|
          key = "#{Reality::Naming.uppercase_constantize(root_project.name)}_REPLICANT_CLIENT_#{Reality::Naming.uppercase_constantize(remote_datasource.name)}_URL"
          environment.setting(key, "http://127.0.0.1:8080/#{Reality::Naming.underscore(remote_datasource.name)}") unless environment.setting?(key)
        end
      end
      if BuildrPlus::FeatureManager.activated?(:remote_references)
        BuildrPlus::RemoteReferences.remote_datasources.each do |remote_datasource|
          key = "#{Reality::Naming.uppercase_constantize(root_project.name)}_REPLICANT_CLIENT_#{Reality::Naming.uppercase_constantize(remote_datasource.name)}_URL"
          environment.setting(key, "http://127.0.0.1:8080/#{Reality::Naming.underscore(remote_datasource.name)}") unless environment.setting?(key)
        end
      end
      if BuildrPlus::FeatureManager.activated?(:gwt)
        prefix = "#{Reality::Naming.uppercase_constantize(root_project.name)}_CODE_SERVER_"
        environment.setting("#{prefix}HOST", 'localhost')
        environment.setting("#{prefix}PORT", '8889')
      end
    end

    def populate_keycloak_remote_client_settings(environment, remote_client)
      prefix = remote_client.redfish_config_prefix
      environment.setting("#{prefix}_REALM", environment.keycloak.realm) unless environment.setting?("#{prefix}_REALM")
      environment.setting("#{prefix}_SERVER_URL", environment.keycloak.base_url) unless environment.setting?("#{prefix}_SERVER_URL")

      # Assume that by default the remote data source uses same keycloak realm but uses this
      # apps client as the remote app is often configured with a bearer only client
      environment.setting("#{prefix}_CLIENT_NAME", BuildrPlus::Keycloak.client_by_client_type(root_project.name).name) unless environment.setting?("#{prefix}_CLIENT_NAME")
      environment.setting("#{prefix}_USERNAME", environment.keycloak.service_username) unless environment.setting?("#{prefix}_USERNAME")
      environment.setting("#{prefix}_PASSWORD", environment.keycloak.service_password) unless environment.setting?("#{prefix}_PASSWORD")
    end

    def populate_keycloak_client_settings(environment, client)
      prefix = client.redfish_config_prefix
      environment.setting("#{prefix}_REALM", environment.keycloak.realm) unless environment.setting?("#{prefix}_REALM")
      environment.setting("#{prefix}_REALM_PUBLIC_KEY", environment.keycloak.public_key) unless environment.setting?("#{prefix}_REALM_PUBLIC_KEY")
      environment.setting("#{prefix}_SERVER_URL", environment.keycloak.base_url) unless environment.setting?("#{prefix}_SERVER_URL")
      environment.setting("#{prefix}_CLIENT_NAME", client.name) unless environment.setting?("#{prefix}_CLIENT_NAME")
    end

    def populate_volume_configuration(environment)
      buildr_project = get_buildr_project.root_project
      if BuildrPlus::FeatureManager.activated?(:redfish)
        domain = Redfish.domain_by_key(buildr_project.name)

        domain.volume_requirements.keys.each do |key|
          environment.set_volume(key, "volumes/#{key}") unless environment.volume?(key)
        end
      end
      base_directory = File.dirname(::Buildr.application.buildfile.to_s)
      environment.volumes.each_pair do |key, local_path|
        environment.set_volume(key, File.expand_path(local_path, base_directory))
      end
    end

    def populate_database_configuration(environment, check_only)
      if !BuildrPlus::FeatureManager.activated?(:dbt) && !environment.databases.empty?
        raise "Databases defined in application configuration but BuildrPlus facet 'dbt' not enabled"
      elsif BuildrPlus::FeatureManager.activated?(:dbt)
        # Ensure all databases are registered in dbt
        environment.databases.each do |database|
          unless ::Dbt.database_for_key?(database.key)
            raise "Database '#{database.key}' defined in application configuration but has not been defined as a dbt database"
          end
        end
        # Create database configurations if in Dbt but configuration does not already exist
        ::Dbt.repository.database_keys.each do |database_key|
          environment.database(database_key) unless environment.database_by_key?(database_key)
        end unless check_only

        buildr_project = get_buildr_project

        environment.databases.each do |database|
          dbt_database = ::Dbt.database_for_key(database.key)
          dbt_imports =
            !dbt_database.imports.empty? ||
              (dbt_database.packaged? && dbt_database.extra_actions.any? { |a| a.to_s =~ /import/ })

          environment.databases.select { |d| d != database }.each do |d|
            ::Dbt.database_for_key(d.key).filters.each do |f|
              if f.is_a?(Struct::DatabaseNameFilter) && f.database_key.to_s == database.key.to_s
                dbt_imports = true
              end
            end
          end unless dbt_imports

          is_sql_server = database.is_a?(BuildrPlus::Config::MssqlDatabaseConfig)
          short_name = Reality::Naming.uppercase_constantize(database.key.to_s == 'default' ? buildr_project.root_project.name : database.key.to_s)
          database.database = "#{BuildrPlus::Config.db_scope}#{short_name}_#{self.env_code(environment.key)}" unless database.database
          database.import_from = "PROD_CLONE_#{short_name}" unless database.import_from || !dbt_imports
          type_prefix = is_sql_server ? 'MS' : 'PG'
          database.host = environment_var("#{type_prefix}_DB_SERVER_HOST") unless database.host
          unless database.port_set?
            port = environment_var("#{type_prefix}_DB_SERVER_PORT", database.port)
            database.port = port.to_i if port
          end
          database.admin_username = environment_var("#{type_prefix}_DB_SERVER_USERNAME") unless database.admin_username
          database.admin_password = environment_var("#{type_prefix}_DB_SERVER_PASSWORD") unless database.admin_password

          if is_sql_server
            database.restore_name = short_name unless database.restore_name
            database.backup_name = short_name unless database.backup_name
            database.backup_location = environment_var("#{type_prefix}_DB_BACKUPS_LOCATION") unless database.backup_location
            database.delete_backup_history = (environment_var("#{type_prefix}_DB_SERVER_DELETE_BACKUP_HISTORY", 'true') == 'true') unless database.delete_backup_history_set?
            unless database.instance
              instance = environment_var("#{type_prefix}_DB_SERVER_INSTANCE", '')
              database.instance = instance unless instance == ''
            end
          end

          raise "Configuration for database key #{database.key} is missing host attribute and can not be derived from environment variable #{type_prefix}_DB_SERVER_HOST" unless database.host
          raise "Configuration for database key #{database.key} is missing admin_username attribute and can not be derived from environment variable #{type_prefix}_DB_SERVER_USERNAME" unless database.admin_username
          raise "Configuration for database key #{database.key} is missing admin_password attribute and can not be derived from environment variable #{type_prefix}_DB_SERVER_PASSWORD" unless database.admin_password
          raise "Configuration for database key #{database.key} specifies import_from but dbt defines no import for database" if database.import_from && !dbt_imports
        end
      end
    end

    def populate_ssrs_configuration(environment, check_only)
      endpoint = BuildrPlus::Config.environment_var('RPTMAN_ENDPOINT')
      domain = BuildrPlus::Config.environment_var('RPTMAN_DOMAIN')
      username = BuildrPlus::Config.environment_var('RPTMAN_USERNAME')
      password = BuildrPlus::Config.environment_var('RPTMAN_PASSWORD')

      if !BuildrPlus::FeatureManager.activated?(:rptman) && environment.ssrs?
        raise "Ssrs defined in application configuration but BuildrPlus facet 'rptman' not enabled"
      elsif BuildrPlus::FeatureManager.activated?(:rptman) && !environment.ssrs? && !check_only
        environment.ssrs
      elsif !BuildrPlus::FeatureManager.activated?(:rptman) && !environment.ssrs?
        return
      elsif BuildrPlus::FeatureManager.activated?(:rptman) && !environment.ssrs? && check_only
        return
      end

      scope = self.app_scope

      environment.ssrs.report_target = endpoint if environment.ssrs.report_target.nil?
      environment.ssrs.domain = domain if environment.ssrs.domain.nil?
      environment.ssrs.admin_username = username if environment.ssrs.admin_username.nil?
      environment.ssrs.admin_password = password if environment.ssrs.admin_password.nil?

      if environment.ssrs.prefix.nil?
        buildr_project = get_buildr_project
        short_name = Reality::Naming.uppercase_constantize(buildr_project.root_project.name)
        environment.ssrs.prefix = "/Auto/#{user || 'NOBODY'}#{scope.nil? ? '' : "_#{scope}"}/#{self.env_code}/#{short_name}"
      end

      raise 'Configuration for ssrs is missing report_target attribute and can not be derived from environment variable RPTMAN_ENDPOINT' unless environment.ssrs.report_target
      raise 'Configuration for ssrs is missing domain attribute and can not be derived from environment variable RPTMAN_DOMAIN' unless environment.ssrs.domain
      raise 'Configuration for ssrs is missing admin_username attribute and can not be derived from environment variable RPTMAN_USERNAME' unless environment.ssrs.admin_username
      raise 'Configuration for ssrs is missing admin_password attribute and can not be derived from environment variable RPTMAN_PASSWORD' unless environment.ssrs.admin_password
    end

    def populate_broker_configuration(environment, check_only)
      if BuildrPlus::FeatureManager.activated?(:jms) && BuildrPlus::FeatureManager.activated?(:docker)
        BuildrPlus::Jms.link_container_to_configuration(BuildrPlus::Config.get_buildr_project, environment)
      end
      if !environment.broker? && !check_only
        host = BuildrPlus::Config.environment_var('OPENMQ_HOST', 'localhost')
        port = BuildrPlus::Config.environment_var('OPENMQ_PORT', 7676)
        username = BuildrPlus::Config.environment_var('OPENMQ_ADMIN_USERNAME', 'admin')
        password = BuildrPlus::Config.environment_var('OPENMQ_ADMIN_PASSWORD', 'admin')

        environment.broker(:host => host, :port => port, :admin_username => username, :admin_password => password)
      end
    end

    def populate_keycloak_configuration(environment, check_only)
      if !BuildrPlus::FeatureManager.activated?(:keycloak) && environment.keycloak?
        raise "Keycloak defined in application configuration but BuildrPlus facet 'keycloak' not enabled"
      elsif BuildrPlus::FeatureManager.activated?(:keycloak) && !environment.keycloak? && !check_only

        base_url = BuildrPlus::Config.environment_var('KEYCLOAK_AUTH_SERVER_URL')
        public_key = BuildrPlus::Config.environment_var('KEYCLOAK_REALM_PUBLIC_KEY')
        realm = BuildrPlus::Config.environment_var('KEYCLOAK_REALM')
        username = BuildrPlus::Config.environment_var('KEYCLOAK_ADMIN_USERNAME')
        password = BuildrPlus::Config.environment_var('KEYCLOAK_ADMIN_PASSWORD')
        service_username = BuildrPlus::Config.environment_var('KEYCLOAK_SERVICE_USERNAME')
        service_password = BuildrPlus::Config.environment_var('KEYCLOAK_SERVICE_PASSWORD')

        environment.keycloak.base_url = base_url if environment.keycloak.base_url.nil?
        environment.keycloak.public_key = public_key if environment.keycloak.public_key.nil?
        environment.keycloak.realm = realm if environment.keycloak.realm.nil?
        environment.keycloak.admin_username = username if environment.keycloak.admin_username.nil?
        environment.keycloak.admin_password = password if environment.keycloak.admin_password.nil?
        environment.keycloak.service_username = service_username if environment.keycloak.service_username.nil?
        environment.keycloak.service_password = service_password if environment.keycloak.service_password.nil?

        raise 'Configuration for keycloak is missing base_url attribute and can not be derived from environment variable KEYCLOAK_AUTH_SERVER_URL' unless environment.keycloak.base_url
        raise 'Configuration for keycloak is missing public_key attribute and can not be derived from environment variable KEYCLOAK_REALM_PUBLIC_KEY' unless environment.keycloak.public_key
        raise 'Configuration for keycloak is missing realm attribute and can not be derived from environment variable KEYCLOAK_REALM' unless environment.keycloak.realm
        raise 'Configuration for keycloak is missing admin_password attribute and can not be derived from environment variable KEYCLOAK_ADMIN_PASSWORD' unless environment.keycloak.admin_password
      end
    end
  end
  f.enhance(:ProjectExtension) do
    after_define do |project|

      if project.ipr?
        project.task(':domgen:all' => ['config:expand_application_yml']) if BuildrPlus::FeatureManager.activated?(:domgen)

        desc 'Generate a complete application configuration from context'
        project.task(':config:expand_application_yml') do
          filename = project._('generated/buildr_plus/config/application.yml')
          trace("Expanding application configuration to #{filename}")
          FileUtils.mkdir_p File.dirname(filename)
          File.open(filename, 'wb') do |file|
            file.write "# DO NOT EDIT: File is auto-generated\n"
            file.write BuildrPlus::Config.application_config.to_h.to_yaml
          end
          database_filename = project._('generated/buildr_plus/config/database.yml')
          trace("Expanding database configuration to #{database_filename}")
          File.open(database_filename, 'wb') do |file|
            file.write "# DO NOT EDIT: File is auto-generated\n"
            file.write BuildrPlus::Config.application_config.to_database_yml.to_yaml
          end
        end
      end
    end
  end
end
