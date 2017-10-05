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

BuildrPlus::Roles.role(:gwt_qa_support, :requires => [:gwt]) do

  project.publish = BuildrPlus::Artifacts.gwt?

  if BuildrPlus::FeatureManager.activated?(:domgen)
    generators = BuildrPlus::Deps.gwt_qa_generators + project.additional_domgen_generators
    Domgen::Build.define_generate_task(generators, :buildr_project => project) do |t|
      t.filter = project.domgen_filter
    end
  end

  compile.with BuildrPlus::Deps.gwt_qa_support_deps

  BuildrPlus::Roles.merge_projects_with_role(project.compile, :gwt)
  BuildrPlus::Roles.merge_projects_with_role(project.compile, :replicant_qa_support)

  package(:jar)
  package(:sources)
end
