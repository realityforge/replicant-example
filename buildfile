require 'buildr_plus'
BuildrPlus::FeatureManager.activate_features([:timerstatus, :replicant])
BuildrPlus::FeatureManager.deactivate_features([:appcache])
BuildrPlus::Idea.peer_projects = ENV['PEER_PROJECTS'] ? ENV['PEER_PROJECTS'].split(',') : %w(replicant)
require 'buildr_plus/projects/java_multimodule'

PACKAGED_DEPS = []

BuildrPlus::Roles.project('tyrell') do
  project.comment = 'A simple application demonstrating the use of the replicant library'
  project.group = 'org.realityforge.tyrell'
end

BuildrPlus::Roles.role(:user_experience) do
  compile.with BuildrPlus::Libs.gwt_lognice
end

require 'buildr_plus/activate'
