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
  Logger = ::Logger.new(STDOUT)
  Logger.level = ::Logger::WARN
  Logger.datetime_format = ''

  def self.error(message)
    Logger.error(message)
    raise message
  end

  class BaseElement
    def initialize(options = {})
      self.options = options
      yield self if block_given?
    end

    def options=(options)
      options.each_pair do |k, v|
        keys = k.to_s.split('.')
        target = self
        keys[0, keys.length - 1].each do |target_accessor_key|
          target = target.send target_accessor_key.to_sym
        end
        target.send "#{keys.last}=", v
      end
    end
  end

  class BaseTaggableElement < BaseElement
    attr_writer :tags

    def tags
      @tags ||= {}
    end

    def description(value)
      tags[:Description] = value
    end

    def tag_as_html(key)
      value = tags[key]
      if value
        require 'maruku' unless defined?(::Maruku)
        ::Maruku.new(value).to_html
      else
        nil
      end
    end
  end

  def self.ParentedElement(parent_key, pre_config_code = '')
    type = Class.new(BaseTaggableElement)
    code = <<-RUBY
    attr_accessor :#{parent_key}

    def initialize(#{parent_key}, options = {}, &block)
      @#{parent_key} = #{parent_key}
      #{pre_config_code}
      super(options, &block)
    end

    def parent
      self.#{parent_key}
    end
    RUBY
    type.class_eval(code)
    type
  end
end
