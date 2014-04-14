module Buildr
  module IntellijIdea
    class IdeaProject

      def add_glassfish_configuration(project, options = {})
        artifact_name = options[:name] || project.iml.id
        domain_name = options[:domain] || project.iml.id
        domain_port = options[:port] || "9009"
        packaged = options[:packaged] || {}
        exploded = options[:exploded] || {}

        add_server_configuration("Glassfish 4.0.0", "GlassfishConfiguration", "Local") do |xml|
          #xml.module(:name => project.iml.id)
          xml.option(:name => "OPEN_IN_BROWSER", :value => "false")
          xml.option(:name => "UPDATING_POLICY", :value => "restart-server")

          xml.deployment() do |deployment|
            packaged.each do |name, deployable|
              artifact = Buildr.artifact(deployable)
              artifact.invoke
              deployment.file(:path => resolve_path(artifact.to_s)) do |file|
                file.settings() do |settings|
                  settings.option(:name => "contextRoot", :value => "/#{name}")
                  settings.option(:name => "defaultContextRoot", :value => "false")
                end
              end
            end
            exploded.each do |deployable_name|
              deployment.artifact(:name => deployable_name) do |artifact|
                artifact.settings()
              end
            end
            #deployment.artifact(:name => artifact_name) do |artifact|
            #  artifact.settings()
            #end
          end

          xml.tag! "server-settings" do |server_settings|
            server_settings.option(:name => "VIRTUAL_SERVER")
            server_settings.option(:name => "DOMAIN", :value => "#{domain_name}")
            server_settings.option(:name => "PRESERVE", :value => "false")
            server_settings.option(:name => "USERNAME", :value => "admin")
            server_settings.option(:name => "PASSWORD", :value => "")
          end

          xml.predefined_log_file(:id => "GlassFish", :enabled => "true")

          xml.extension(:name => "coverage", :enabled => "false", :merge => "false", :sample_coverage => "true", :runner => "idea")

          xml.RunnerSettings(:RunnerId => "Cover")

          add_runner_settings(xml, "Cover")
          add_configuration_wrapper(xml, "Cover")

          add_runner_settings(xml, "Debug", {
            :DEBUG_PORT => "#{domain_port}",
           :TRANSPORT => "0",
            :LOCAL => "true",
          })
          add_configuration_wrapper(xml, "Debug")

          add_runner_settings(xml, "Run")
          add_configuration_wrapper(xml, "Run")

          xml.method() do |method|
            method.option(:name => "BuildArtifacts", :enabled => "true") do |option|
              option.artifact(:name => artifact_name)
            end
          end
        end
      end

      private

      def add_runner_settings(xml, name, options = {})
        xml.RunnerSettings(:RunnerId => "#{name}") do |runner_settings|
          options.each do |name, value|
            runner_settings.option(:name => "#{name}", :value => "#{value}")
          end
        end
      end

      def add_configuration_wrapper(xml, name)
        xml.ConfigurationWrapper(:VM_VAR => "JAVA_OPTS", :RunnerId => "#{name}") do |configuration_wrapper|
          configuration_wrapper.option(:name => "USE_ENV_VARIABLES", :value => "true")
          configuration_wrapper.STARTUP() do |startup|
            startup.option(:name => "USE_DEFAULT", :value => "true")
            startup.option(:name => "SCRIPT", :value => "")
            startup.option(:name => "VM_PARAMETERS", :value => "")
            startup.option(:name => "PROGRAM_PARAMETERS", :value => "")
          end
          configuration_wrapper.SHUTDOWN() do |shutdown|
            shutdown.option(:name => "USE_DEFAULT", :value => "true")
            shutdown.option(:name => "SCRIPT", :value => "")
            shutdown.option(:name => "VM_PARAMETERS", :value => "")
            shutdown.option(:name => "PROGRAM_PARAMETERS", :value => "")
          end
        end
      end

      def add_server_configuration(name, type, factory_name, server_name = "Glassfish 4.0.0", default = false)
        add_to_composite_component(self.configurations) do |xml|
          xml.configuration(:name => name, :type => type, :factoryName => factory_name, :default => default, :APPLICATION_SERVER_NAME => server_name) do |xml|
            yield xml if block_given?
          end
        end
      end
    end
  end
end

