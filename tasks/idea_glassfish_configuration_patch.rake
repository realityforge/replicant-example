raise "Patch applied in this version of Buildr" unless Buildr::VERSION == '1.4.20'

module Buildr
  module IntellijIdea
    class IdeaProject

      def add_glassfish_configuration(project, options = {})
        artifact_name = options[:name] || project.iml.id
        version = options[:version] || '4.1.0'
        server_name = options[:server_name] || "GlassFish #{version}"
        domain_name = options[:domain] || project.iml.id
        domain_port = options[:port] || '9009'
        packaged = options[:packaged] || {}
        exploded = options[:exploded] || {}

        add_to_composite_component(self.configurations) do |xml|
          xml.configuration(:name => server_name, :type => 'GlassfishConfiguration', :factoryName => 'Local', :default => false, :APPLICATION_SERVER_NAME => server_name) do |xml|
            xml.option(:name => 'OPEN_IN_BROWSER', :value => 'false')
            xml.option(:name => 'UPDATING_POLICY', :value => 'restart-server')

            xml.deployment do |deployment|
              packaged.each do |name, deployable|
                artifact = Buildr.artifact(deployable)
                artifact.invoke
                deployment.file(:path => resolve_path(artifact.to_s)) do |file|
                  file.settings do |settings|
                    settings.option(:name => 'contextRoot', :value => "/#{name}")
                    settings.option(:name => 'defaultContextRoot', :value => 'false')
                  end
                end
              end
              exploded.each do |deployable_name|
                deployment.artifact(:name => deployable_name) do |artifact|
                  artifact.settings
                end
              end
            end

            xml.tag! 'server-settings' do |server_settings|
              server_settings.option(:name => 'VIRTUAL_SERVER')
              server_settings.option(:name => 'DOMAIN', :value => domain_name.to_s)
              server_settings.option(:name => 'PRESERVE', :value => 'false')
              server_settings.option(:name => 'USERNAME', :value => 'admin')
              server_settings.option(:name => 'PASSWORD', :value => '')
            end

            xml.predefined_log_file(:id => 'GlassFish', :enabled => 'true')

            xml.extension(:name => 'coverage', :enabled => 'false', :merge => 'false', :sample_coverage => 'true', :runner => 'idea')

            xml.RunnerSettings(:RunnerId => 'Cover')

            add_glassfish_runner_settings(xml, 'Cover')
            add_glassfish_configuration_wrapper(xml, 'Cover')

            add_glassfish_runner_settings(xml, 'Debug', {
              :DEBUG_PORT => domain_port.to_s,
              :TRANSPORT => '0',
              :LOCAL => 'true',
            })
            add_glassfish_configuration_wrapper(xml, 'Debug')

            add_glassfish_runner_settings(xml, 'Run')
            add_glassfish_configuration_wrapper(xml, 'Run')

            xml.method do |method|
              method.option(:name => 'BuildArtifacts', :enabled => 'true') do |option|
                option.artifact(:name => artifact_name)
              end
            end
          end
        end
      end
    end
  end
end
