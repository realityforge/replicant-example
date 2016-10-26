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

module Domgen
  class << self
    def resolve_artifact(artifact_spec)
      parts = artifact_spec.split(':')
      parts << 'jar' if parts.size == 2

      artifact_spec_prefix = parts.join(':')

      artifacts = ::Buildr::ArtifactNamespace.root.values.select do |a|
        a.to_spec =~ /^#{Regexp.escape(artifact_spec_prefix)}\:/ &&
          a.to_spec.split(':').size == 4
      end

      raise "Attempting to resolve artifact '#{artifact_spec}' resulted in no artifacts." if 0 == artifacts.size
      raise "Attempting to resolve artifact '#{artifact_spec}' resulted in multiple artifacts - #{artifacts.collect { |a| a.to_spec }.inspect}." if 1 != artifacts.size

      a = ::Buildr.artifact(artifacts[0].to_spec)
      a.invoke
      a.to_s
    end

    def resolve_file(filename)
      Rake::FileTask.define_task(filename).invoke
      filename
    end
  end
end
