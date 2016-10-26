require 'buildr/git_auto_version'
require 'buildr/gwt'
require 'buildr/top_level_generate_dir'

GWT_DEPS = [:gwt_user,
            :gwt_lognice,
            :gwt_webpoller,
            :gwt_property_source,
            :google_guice,
            :google_guice_assistedinject,
            :aopalliance,
            :gwt_gin]
JACKSON_DEPS = [:jackson_core, :jackson_mapper, :jackson_annotations]
PROVIDED_DEPS = [:javax_jsr305, :findbugs_annotations, :javax_javaee] + GWT_DEPS
COMPILE_DEPS = [:replicant, :gwt_servlet, :simple_session_filter, :field_filter, :gwt_cache_filter, :gwt_datatypes] + JACKSON_DEPS
PACKAGE_DEPS = COMPILE_DEPS

desc 'A simple application demonstrating the use of the replicant library'
define 'replicant-example' do
  project.group = 'org.realityforge.replicant.example'

  compile.options.source = '1.7'
  compile.options.target = '1.7'
  compile.options.lint = 'all'

  # Expanded "imit" template_set to avoid bringing in test classes
  Domgen::Build.define_generate_task([:ee, :jaxrs, :ee_web_xml, :ee_beans_xml, :gwt_client_app, :gwt_client_gwt_modules, :gwt_client_module, :gwt, :gwt_rpc, :imit_shared, :imit_server_service, :imit_server_entity, :imit_client_service, :imit_client_entity, :jackson_date_util])

  compile.with COMPILE_DEPS, PROVIDED_DEPS

  # Unfortunately buildr does not gracefully handle resource directories not being present
  # when project processed so we collect extra dependencies by looking at the generated directories
  extra_deps = project.iml.main_generated_resource_directories.flatten.compact.collect do |a|
    a.is_a?(String) ? file(a) : a
  end + project.iml.main_generated_source_directories.flatten.compact.collect do |a|
    a.is_a?(String) ? file(a) : a
  end

  dependencies = project.compile.dependencies + [project.compile.target] + extra_deps
  gwt_dir = gwt(%w(org.realityforge.replicant.example.Tyrell),
                :java_args => %w(-Xms512M -Xmx1024M -XX:PermSize=128M -XX:MaxPermSize=256M),
                :dependencies => dependencies) unless ENV['GWT'] == 'no'

  test.using :testng
  test.with :mockito

  package(:war).tap do |war|
    war.libs = PACKAGE_DEPS
  end

  clean { rm_rf "#{File.dirname(__FILE__)}/artifacts" }

  iml.add_gwt_facet({'org.realityforge.replicant.example.modules.TyrellDev' => false,
                     'org.realityforge.replicant.example.Tyrell' => false},
                    :settings => {:compilerMaxHeapSize => '1024'},
                    :gwt_dev_artifact => :gwt_dev)

  # Hacke to remove GWT from path
  webroots = {}
  webroots[_(:source, :main, :webapp)] = '/'
  webroots[_(:source, :main, :webapp_local)] = '/'
  assets.paths.each { |path| webroots[path.to_s] = '/' if path.to_s != gwt_dir.to_s }
  iml.add_web_facet(:webroots => webroots)

  iml.add_jpa_facet
  iml.add_ejb_facet
  iml.add_jruby_facet

  ipr.add_exploded_war_artifact(project,
                                :build_on_make => true,
                                :enable_gwt => true,
                                :enable_jpa => true,
                                :enable_ejb => true,
                                :enable_war => true,
                                :dependencies => [project, PACKAGE_DEPS])

  ipr.add_glassfish_configuration(project, :server_name => 'GlassFish 4.1.1.162', :domain => 'tyrell', :exploded => [project.name])

  ipr.add_component_from_artifact(:idea_codestyle)

  ipr.add_gwt_configuration(project,
                            :gwt_module => 'org.realityforge.replicant.example.ExampleDev',
                            :vm_parameters => '-Xmx3G',
                            :shell_parameters => "-port 8888 -war #{_(:artifacts, project.name)}/",
                            :launch_page => 'http://127.0.0.1:8080/replicant-example')

  ipr.extra_modules << '../replicant/replicant.iml'
end
