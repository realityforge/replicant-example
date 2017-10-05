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

BuildrPlus::FeatureManager.feature(:syncrecord => [:appconfig]) do |f|
  f.enhance(:Config) do
    attr_writer :include_db_artifact

    def include_db_artifact?
      @include_db_artifact.nil? ? true : !!@include_db_artifact
    end

    def syncrecord_db
      pg_suffix? ? :syncrecord_db_pg : :syncrecord_db
    end

    def syncrecord_server
      pg_suffix? ? :syncrecord_server_pg : :syncrecord_server
    end

    def syncrecord_server_qa
      pg_suffix? ? :syncrecord_server_qa_pg : :syncrecord_server_qa
    end

    def syncrecord_qa
      pg_suffix? ? :syncrecord_qa_pg : :syncrecord_qa
    end

    def syncrecord_rest_client
      pg_suffix? ? :syncrecord_rest_client_pg : :syncrecord_rest_client
    end

    def pg_suffix?
      BuildrPlus::Db.is_multi_database_project? && BuildrPlus::Db.pgsql?
    end
  end
end
