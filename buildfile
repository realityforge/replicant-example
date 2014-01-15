require 'buildr/git_auto_version'
require 'buildr/top_level_generate_dir'

GWT_DEPS = [:gwt_datatypes,
            :google_guice,
            :google_guice_assistedinject,
            :aopalliance,
            :gwt_gin,
            :javax_validation_sources]
PROVIDED_DEPS = [:gwt_websockets, :javax_annotation, :javax_javaee] + GWT_DEPS
COMPILE_DEPS = [:gwt_user, :replicant, :jackson_core, :jackson_mapper]
PACKAGE_DEPS = [:gwt_cache_filter] + COMPILE_DEPS

desc "A simple application demonstrating the use of the replicant library"
define 'replicant-example' do
  project.group = 'org.realityforge.replicant.example'

  compile.options.source = '1.7'
  compile.options.target = '1.7'
  compile.options.lint = 'all'

  Domgen::GenerateTask.new(:Tyrell,
                           "server",
                           [:ee, :gwt, :gwt_rpc, :imit],
                           _(:target, :generated, "domgen"))


  compile.with COMPILE_DEPS, PROVIDED_DEPS

  gwt_dir = gwt(["org.realityforge.replicant.example.Example"],
                :java_args => ["-Xms512M", "-Xmx1024M", "-XX:PermSize=128M", "-XX:MaxPermSize=256M"],
                :draft_compile => (ENV["FAST_GWT"] == 'true'),
                :dependencies => [:javax_validation, :javax_validation_sources] + project.compile.dependencies)

  test.with :mockito

  package(:war).tap do |war|
    war.libs.clear
    war.libs.concat PACKAGE_DEPS
  end

  clean { rm_rf "#{File.dirname(__FILE__)}/artifacts" }

  iml.add_gwt_facet({'org.realityforge.replicant.example.Example' => true},
                    :settings => {:compilerMaxHeapSize => "1024"},
                    :gwt_dev_artifact => :gwt_dev)

  # Hacke to remove GWT from path
  webroots = {}
  webroots[_(:source, :main, :webapp)] = "/" if File.exist?(_(:source, :main, :webapp))
  assets.paths.each { |path| webroots[path.to_s] = "/" if path.to_s != gwt_dir.to_s }
  iml.add_web_facet(:webroots => webroots)

  iml.add_jruby_facet

  ipr.add_exploded_war_artifact(project,
                                :build_on_make => true,
                                :enable_gwt => true,
                                :enable_war => true,
                                :dependencies => [project, PACKAGE_DEPS])
end
