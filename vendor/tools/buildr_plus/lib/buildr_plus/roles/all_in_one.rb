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

BuildrPlus::Roles.role(:all_in_one) do
  project.publish = true

  if BuildrPlus::FeatureManager.activated?(:domgen)
    generators = BuildrPlus::Deps.server_generators + project.additional_domgen_generators
    if BuildrPlus::FeatureManager.activated?(:db)
      generators << [:jpa, :jpa_test_orm_xml, :jpa_test_persistence_xml]
      generators << [:jpa_test_qa, :jpa_test_qa_external, :jpa_ejb_dao, :jpa_dao_test] if BuildrPlus::FeatureManager.activated?(:ejb)
      generators << [:imit_server_entity_listener, :imit_server_entity_replication] if BuildrPlus::FeatureManager.activated?(:replicant)
    end

    generators << [:redfish_fragment] if BuildrPlus::FeatureManager.activated?(:redfish)
    generators << [:keycloak_client_config] if BuildrPlus::FeatureManager.activated?(:keycloak)

    generators += project.additional_domgen_generators

    Domgen::Build.define_generate_task(generators.flatten, :buildr_project => project) do |t|
      t.filter = project.domgen_filter
    end
  end

  compile.with BuildrPlus::Deps.server_deps

  test.with BuildrPlus::Deps.model_qa_support_deps

  package(:war).tap do |war|
    war.libs.clear
    # Findbugs+jetbrains libs added otherwise CDI scanning slows down due to massive number of ClassNotFoundExceptions
    war.libs << BuildrPlus::Deps.findbugs_provided
    war.libs << BuildrPlus::Deps.jetbrains_annotations
    war.libs << BuildrPlus::Deps.server_compile_deps
    war.exclude project.less_path if BuildrPlus::FeatureManager.activated?(:less)
    if BuildrPlus::FeatureManager.activated?(:sass)
      project.sass_paths.each do |sass_path|
        war.exclude project._(sass_path)
      end
    end
    project.assets.paths.each do |asset_dir|
      war.include asset_dir, :as => '.'
    end
  end

  iml.add_jpa_facet if BuildrPlus::FeatureManager.activated?(:db)
  iml.add_ejb_facet if BuildrPlus::FeatureManager.activated?(:ejb)

  webroots = {}
  webroots[_(:source, :main, :webapp)] = '/'
  webroots[_(:source, :main, :webapp_local)] = '/' if BuildrPlus::FeatureManager.activated?(:gwt) && BuildrPlus::FeatureManager.activated?(:user_experience)
  assets.paths.each do |path|
    next if path.to_s =~ /generated\/gwt\// && BuildrPlus::FeatureManager.activated?(:gwt)
    next if path.to_s =~ /generated\/less\// && BuildrPlus::FeatureManager.activated?(:less)
    next if path.to_s =~ /generated\/sass\// && BuildrPlus::FeatureManager.activated?(:sass)
    webroots[path.to_s] = '/'
  end
  iml.add_web_facet(:webroots => webroots)

  default_testng_args = []
  default_testng_args << '-ea'
  default_testng_args << '-Xmx2024M'

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

  default_testng_args.concat(BuildrPlus::Glassfish.addtional_default_testng_args)

  ipr.add_default_testng_configuration(:jvm_args => default_testng_args.join(' '))

  dependencies = [project]
  # Findbugs+jetbrains libs added otherwise CDI scanning slows down due to massive number of ClassNotFoundExceptions
  dependencies << BuildrPlus::Deps.findbugs_provided
  dependencies << BuildrPlus::Deps.jetbrains_annotations
  dependencies << BuildrPlus::Deps.server_compile_deps

  war_module_names = [project.iml.name]
  jpa_module_names = BuildrPlus::FeatureManager.activated?(:db) ? [project.iml.name] : []
  ejb_module_names =
    BuildrPlus::FeatureManager.activated?(:db) || BuildrPlus::FeatureManager.activated?(:ejb) ? [project.iml.name] : []

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
end
