require 'buildr_plus'
BuildrPlus::FeatureManager.activate_features([:timerstatus, :replicant])
BuildrPlus::FeatureManager.deactivate_features([:appcache])
BuildrPlus::Idea.peer_projects = ENV['PEER_PROJECTS'] ? ENV['PEER_PROJECTS'].split(',') : %w(replicant)
BuildrPlus::Gwt.enable_gwt_js_exports = true
require 'buildr_plus/projects/java_multimodule'

PACKAGED_DEPS = []

BuildrPlus::Roles.project('tyrell') do
  project.comment = 'A simple application demonstrating the use of the replicant library'
  project.group = 'org.realityforge.tyrell'
end

BuildrPlus::Roles.role(:user_experience) do
  compile.with BuildrPlus::Libs.gwt_lognice,
               BuildrPlus::Libs.jsinterop_base,
               BuildrPlus::Libs.braincheck,
               BuildrPlus::Libs.elemental2_dom,
               BuildrPlus::Libs.elemental2_promise,
               :react4j_annotations,
               :react4j_core,
               :react4j_dom,
               :react4j_arez,
               :react4j_processor,
               :react4j_widget,
               :arez_annotations,
               :arez_core,
               :arez_processor,
               :arez_extras,
               :arez_browser_extras,
               :javapoet,
               :guava
end

require 'buildr_plus/activate'
