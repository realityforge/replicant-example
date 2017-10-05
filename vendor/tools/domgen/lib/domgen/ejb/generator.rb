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

Domgen::Generator.define([:ejb],
                         "#{File.dirname(__FILE__)}/templates",
                         [Domgen::Java::Helper, Domgen::JAXB::Helper]) do |g|
  g.template_set(:ejb_services) do |template_set|
    template_set.erb_template(:service,
                              'service.java.erb',
                              'main/java/#{service.ejb.qualified_service_name.gsub(".","/")}.java')
    template_set.erb_template(:data_module,
                              'service_package_info.java.erb',
                              'main/java/#{data_module.ejb.server_service_package.gsub(".","/")}/package-info.java',
                              :guard => 'data_module.services.any?{|e|e.ejb?}')
  end
  g.template_set(:ejb_service_facades => [:ejb_services]) do |template_set|
    template_set.erb_template(:service,
                              'boundary_service.java.erb',
                              'main/java/#{service.ejb.qualified_boundary_interface_name.gsub(".","/")}.java',
                              :guard => 'service.ejb.generate_boundary?')
    template_set.erb_template(:service,
                              'remote_service.java.erb',
                              'main/java/#{service.ejb.qualified_remote_service_name.gsub(".","/")}.java',
                              :guard => 'service.ejb.generate_boundary? && service.ejb.remote?')
    template_set.erb_template(:service,
                              'boundary_implementation.java.erb',
                              'main/java/#{service.ejb.qualified_boundary_implementation_name.gsub(".","/")}.java',
                              :guard => 'service.ejb.generate_boundary?')
    template_set.erb_template(:method,
                              'scheduler.java.erb',
                              'main/java/#{method.ejb.qualified_scheduler_name.gsub(".","/")}.java',
                              :guard => 'method.ejb.schedule?')
  end
  g.template_set(:ejb_glassfish_config_assets) do |template_set|
    template_set.erb_template(:repository,
                              'glassfish_ejb.xml.erb',
                              'main/webapp/WEB-INF/glassfish-ejb-jar.xml',
                              :name => 'WEB-INF/glassfish-ejb-jar.xml')
  end
  g.template_set(:ejb_glassfish_config_resources) do |template_set|
    template_set.erb_template(:repository,
                              'glassfish_ejb.xml.erb',
                              'main/resources/META-INF/glassfish-ejb-jar.xml',
                              :name => 'META-INF/glassfish-ejb-jar.xml')
  end

  %w(main test).each do |type|
    g.template_set("ejb_#{type}_qa_external") do |template_set|
      template_set.description = 'Quality Assurance/Test classes shared outside the project'
      template_set.erb_template(:repository,
                                'services_module.java.erb',
                                type + '/java/#{repository.ejb.qualified_services_module_name.gsub(".","/")}.java')
      template_set.erb_template(:repository,
                                'complete_module.java.erb',
                                type + '/java/#{repository.ejb.qualified_complete_module_name.gsub(".","/")}.java')
    end
    g.template_set("ejb_#{type}_qa") do |template_set|
      template_set.description = 'Quality Assurance/Test classes shared within the project'
      template_set.erb_template(:repository,
                                'abstract_service_test.java.erb',
                                type + '/java/#{repository.ejb.qualified_abstract_service_test_name.gsub(".","/")}.java')
      template_set.erb_template(:repository,
                                'base_service_test.java.erb',
                                type + '/java/#{repository.ejb.qualified_base_service_test_name.gsub(".","/")}.java',
                                :guard => '!repository.ejb.custom_base_service_test?')
      template_set.erb_template(:repository,
                                'cdi_types_test.java.erb',
                                type + '/java/#{repository.ejb.qualified_cdi_types_test_name.gsub(".","/")}.java',
                                :guard => 'repository.ee.use_cdi?')
    end
    g.template_set(:"ejb_#{type}_qa_aggregate") do |template_set|
      template_set.erb_template(:repository,
                                'aggregate_service_test.java.erb',
                                type + '/java/#{repository.ejb.qualified_aggregate_service_test_name.gsub(".","/")}.java')
    end
  end

  g.template_set(:ejb_test_service_test) do |template_set|
    template_set.erb_template(:service,
                              'service_test.java.erb',
                              'test/java/#{service.ejb.qualified_service_test_name.gsub(".","/")}.java',
                              :guard => 'service.ejb.generate_base_test?')
  end

  g.template_set(:ejb => [:ejb_service_facades, :jpa_ejb_dao])
end
