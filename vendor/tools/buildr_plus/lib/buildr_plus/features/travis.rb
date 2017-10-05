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

# Enable this feature if the code is tested using travis
BuildrPlus::FeatureManager.feature(:travis => [:oss]) do |f|
  f.enhance(:Config) do

    def travis_content
      rv = BuildrPlus::Ruby.ruby_version
      docker_active = BuildrPlus::FeatureManager.activated?(:docker)

      content = <<CONTENT
# DO NOT EDIT: File is auto-generated
language: ruby
jdk:
  - oraclejdk#{BuildrPlus::Java.version}
sudo: #{docker_active ? 'required' : 'false'}
rvm:
  - #{rv}
CONTENT
      if docker_active
        content += <<CONTENT
services:
  - docker
CONTENT
      end
      content += <<CONTENT
addons:
  apt:
    packages:
CONTENT
      if BuildrPlus::Db.tiny_tds_defined?
        content += <<CONTENT
    - freetds-dev
CONTENT
      end
      if docker_active
        content += <<CONTENT
    - socat
CONTENT
      end
      content += <<CONTENT
install:
  - rvm use #{rv}
  - gem install bundler
  - bundle install
CONTENT
      if BuildrPlus::FeatureManager.activated?(:node)
        nv = BuildrPlus::Node.node_version
        content += <<CONTENT
  - nvm install v#{nv}
  - nvm use v#{nv}
CONTENT
        if BuildrPlus::Node.root_package_json_present?
          content += <<CONTENT
  - npm install -g yarn
  - yarn install
CONTENT
        end
      end

      if BuildrPlus::Db.is_multi_database_project? || BuildrPlus::Db.pg_defined?
        content += <<CONTENT
  - export DB_TYPE=pg
  - export PG_DB_SERVER_USERNAME=postgres
  - export PG_DB_SERVER_PASSWORD=postgres
CONTENT
        if docker_active
          # We need to introduce a local port proxy and expose it on public ip so that
          # any code inside the container can access the postgres server
          content += <<CONTENT
  - export HOST_IP_ADDRESS=`ifconfig eth0 | sed -En 's/127.0.0.1//;s/.*inet (addr:)?(([0-9]*\\.){3}[0-9]*).*/\\2/p'`
  - socat TCP-LISTEN:10000,fork TCP:127.0.0.1:5432 &
  - export PG_DB_SERVER_HOST=${HOST_IP_ADDRESS}
  - export PG_DB_SERVER_PORT=10000
CONTENT
        else
          content += <<CONTENT
  - export PG_DB_SERVER_HOST=127.0.0.1
CONTENT
        end
      end

      if BuildrPlus::FeatureManager.activated?(:keycloak)
        content += <<CONTENT
  - export KEYCLOAK_REALM=MyRealm
  - export KEYCLOAK_REALM_PUBLIC_KEY="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAic34zD00ZQmia/O4peUJyO1g9lnY/p9vi+dxfbFdAilsbsHj2gfFuxiPInk75yIZR7C7DPNS34PWhA6e4EWuero0MhyzyBDM8IS2577EgdlCtPnANbqe4HI/k6Zm/rd3liwl44tVD3Z9Yv7Ax4h7ChvDTYqFojeD5SE1cNK67MjNWCdlQbudSayUKetSK/PDuBUTNdHxoyqvWrUl+r5dO1XH+ItyliSdThFI9rcGuDWZWfNxMCHmLlDFAnPiYUuUWXkS3EpPCNN2RVAao7yb5ZJ52ijZKqftht7Cu4NwaTjgtyhbvvQQ7W6nhRtQJEt4+eD6KLTUAQLtOvHRtNkfrwIDAQAB"
  - export KEYCLOAK_AUTH_SERVER_URL="http://id.example.com/"
  - export KEYCLOAK_ADMIN_PASSWORD=secret
  - export KEYCLOAK_SERVICE_USERNAME=MyUsername
  - export KEYCLOAK_SERVICE_PASSWORD=MyPassword
CONTENT
      end

      content += <<CONTENT
script: buildr ci:pull_request
git:
  depth: 10
CONTENT

      content
    end

  end
  f.enhance(:ProjectExtension) do
    task 'travis:check' do
      base_directory = File.dirname(Buildr.application.buildfile.to_s)
      filename = "#{base_directory}/.travis.yml"
      if !File.exist?(filename) || IO.read(filename) != BuildrPlus::Travis.travis_content
        raise 'The .travis.yml configuration file does not exist or is not up to date. Please run "buildr travis:fix" and commit changes.'
      end
    end

    task 'travis:fix' do
      base_directory = File.dirname(Buildr.application.buildfile.to_s)
      filename = "#{base_directory}/.travis.yml"
      File.open(filename, 'wb') do |file|
        file.write BuildrPlus::Travis.travis_content
      end
    end
  end
end
