# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with this
# work for additional information regarding copyright ownership.  The ASF
# licenses this file to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
# License for the specific language governing permissions and limitations under
# the License.

raise 'Patch applied in latest release of buildr' if Buildr::VERSION > '1.4.23'

module Buildr
  module ActivateJrubyFacet
    module ProjectExtension
      include Extension

      # A custom extension that just enables the jruby facet in IDEA projects for all projects
      # that generate idea projects files. This is useful as buildr/rake scripts and other automation
      # can be identified as ruby projects.
      after_define do |project|
        project.iml.add_jruby_facet if project.iml?
      end
    end
  end
end

class Buildr::Project
  include Buildr::ActivateJrubyFacet::ProjectExtension
end
