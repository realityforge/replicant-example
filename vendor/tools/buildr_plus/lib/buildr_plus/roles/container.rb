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

BuildrPlus::Roles.role(:container) do

  if BuildrPlus::FeatureManager.activated?(:domgen)
    generators = []

    generators << [:redfish_fragment] if BuildrPlus::FeatureManager.activated?(:redfish)
    generators << [:keycloak_client_config] if BuildrPlus::FeatureManager.activated?(:keycloak)

    generators += project.additional_domgen_generators

    Domgen::Build.define_generate_task(generators.flatten, :buildr_project => project) do |t|
      t.filter = project.domgen_filter
    end unless generators.empty?
  end

  project.publish = false

  default_testng_args = []
  default_testng_args << '-ea'
  default_testng_args << '-Xmx2024M'

  if BuildrPlus::Roles.project_with_role?(:integration_tests)
    server_project = project(BuildrPlus::Roles.project_with_role(:server).name)
    war_package = server_project.package(:war)
    war_dir = File.dirname(war_package.to_s)

    default_testng_args << "-Dembedded.glassfish.artifacts=#{BuildrPlus::Guiceyloops.glassfish_spec_list}"
    default_testng_args << "-Dwar.dir=#{war_dir}"
    BuildrPlus::Integration.additional_applications_to_deploy.each do |key, artifact|
      default_testng_args << "-D#{key}.war.filename=#{Buildr.artifact(artifact).to_s}"
    end
  end

  if BuildrPlus::FeatureManager.activated?(:db)
    default_testng_args << "-javaagent:#{Buildr.artifact(BuildrPlus::Libs.eclipselink).to_s}" unless BuildrPlus::FeatureManager.activated?(:gwt)

    if BuildrPlus::FeatureManager.activated?(:dbt)
      BuildrPlus::Config.load_application_config! if BuildrPlus::FeatureManager.activated?(:config)
      Dbt.repository.load_configuration_data

      Dbt.database_keys.each do |database_key|
        next if BuildrPlus::Dbt.manual_testing_only_database?(database_key)

        prefix = Dbt::Config.default_database?(database_key) ? '' : "#{database_key}."
        database = Dbt.configuration_for_key(database_key, :test)
        default_testng_args << "-D#{prefix}test.db.url=#{database.build_jdbc_url(:credentials_inline => true)}"
        default_testng_args << "-D#{prefix}test.db.name=#{database.catalog_name}"
      end
    end
  end

  if BuildrPlus::FeatureManager.activated?(:keycloak)
    environment = BuildrPlus::Config.application_config.environment_by_key(:test)
    default_testng_args << "-Dkeycloak.server-url=#{environment.keycloak.base_url}"
    default_testng_args << "-Dkeycloak.public-key=#{environment.keycloak.public_key}"
    default_testng_args << "-Dkeycloak.realm=#{environment.keycloak.realm}"
    default_testng_args << "-Dkeycloak.service_username=#{environment.keycloak.service_username}"
    default_testng_args << "-Dkeycloak.service_password=#{environment.keycloak.service_password}"
    BuildrPlus::Keycloak.clients.each do |client|
      default_testng_args << "-D#{client.client_type}.keycloak.client=#{client.auth_client.name('test')}"
    end
  end

  default_testng_args.concat(BuildrPlus::Glassfish.addtional_default_testng_args)

  ipr.add_default_testng_configuration(:jvm_args => default_testng_args.join(' '))

  # Need to use definitions as projects have yet to be when resolving
  # container project which is typically the root project
  if BuildrPlus::Roles.project_with_role?(:server)
    server_project = project(BuildrPlus::Roles.project_with_role(:server).name)
    model_project =
      BuildrPlus::Roles.project_with_role?(:model) ?
        project(BuildrPlus::Roles.project_with_role(:model).name) :
        nil
    shared_project =
      BuildrPlus::Roles.project_with_role?(:shared) ?
        project(BuildrPlus::Roles.project_with_role(:shared).name) :
        nil

    dependencies = [server_project, model_project, shared_project].compact
    # Findbugs+jetbrains libs added otherwise CDI scanning slows down due to massive number of ClassNotFoundExceptions
    dependencies << BuildrPlus::Deps.findbugs_provided
    dependencies << BuildrPlus::Deps.jetbrains_annotations
    dependencies << BuildrPlus::Deps.server_compile_deps

    war_module_names = [server_project.iml.name]
    jpa_module_names = []
    jpa_module_names << model_project.iml.name if model_project

    ejb_module_names = [server_project.iml.name]
    ejb_module_names << model_project.iml.name if model_project

    ipr.add_exploded_war_artifact(project,
                                  :dependencies => dependencies,
                                  :war_module_names => war_module_names,
                                  :jpa_module_names => jpa_module_names,
                                  :ejb_module_names => ejb_module_names)

    remote_packaged_apps = BuildrPlus::Glassfish.remote_only_packaged_apps.dup.merge(BuildrPlus::Glassfish.packaged_apps)
    local_packaged_apps = BuildrPlus::Glassfish.non_remote_only_packaged_apps.dup.merge(BuildrPlus::Glassfish.packaged_apps)

    local_packaged_apps['greenmail'] = BuildrPlus::Libs.greenmail_server if BuildrPlus::FeatureManager.activated?(:mail)

    ipr.add_glassfish_remote_configuration(project,
                                           :server_name => 'GlassFish 4.1.2.172',
                                           :exploded => [project.name],
                                           :packaged => remote_packaged_apps)
    unless BuildrPlus::Redfish.local_domain_update_only?
      ipr.add_glassfish_configuration(project,
                                      :server_name => 'GlassFish 4.1.2.172',
                                      :exploded => [project.name],
                                      :packaged => local_packaged_apps)

      if local_packaged_apps.size > 0
        only_packaged_apps = BuildrPlus::Glassfish.only_only_packaged_apps.dup
        ipr.add_glassfish_configuration(project,
                                        :configuration_name => "#{Reality::Naming.pascal_case(project.name)} Only - GlassFish 4.1.2.172",
                                        :server_name => 'GlassFish 4.1.2.172',
                                        :exploded => [project.name],
                                        :packaged => only_packaged_apps)
      end
    end

    BuildrPlus::Roles.buildr_projects_with_role(:user_experience).each do |p|
      gwt_modules = p.determine_top_level_gwt_modules('Dev')
      gwt_modules.each do |gwt_module|
        short_name = gwt_module.gsub(/.*\.([^.]+)Dev$/, '\1')
        path = short_name.gsub(/^#{Reality::Naming.pascal_case(project.name)}/, '')
        if path.size > 0
          server_project = project(BuildrPlus::Roles.project_with_role(:server).name)
          %w(html jsp).each do |extension|
            candidate = "#{Reality::Naming.underscore(path)}.#{extension}"
            path = candidate if File.exist?(server_project._(:source, :main, :webapp_local, candidate))
          end
        end
        iml.excluded_directories << project._('tmp/gwt')
        ipr.add_gwt_configuration(p,
                                  :gwt_module => gwt_module,
                                  :start_javascript_debugger => false,
                                  :vm_parameters => "-Xmx3G -Djava.io.tmpdir=#{_('tmp/gwt')}",
                                  :shell_parameters => "#{BuildrPlus::Gwt.enable_gwt_js_exports? ? '-generateJsInteropExports ' : ''}-port 8888 -codeServerPort 8889 -bindAddress 0.0.0.0 -war #{_(:generated, 'gwt-export')}/",
                                  :launch_page => "http://127.0.0.1:8080/#{p.root_project.name}/#{path}")
      end
    end
  end
end
