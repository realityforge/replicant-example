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
  module Imit

    class ImitationAttributeInverse < Domgen.ParentedElement(:inverse)
      def traversable=(traversable)
        Domgen.error("traversable #{traversable} is invalid") unless inverse.class.inverse_traversable_types.include?(traversable)
        @traversable = traversable
      end

      def traversable?
        @traversable.nil? ? (self.inverse.traversable? && self.inverse.attribute.referenced_entity.imit?) : @traversable
      end

      def exclude_edges
        @exclude_edges ||= []
      end

      def exclude_edges=(exclude_edges)
        @exclude_edges = exclude_edges
      end

      def replication_edges=(replication_edges)
        raise "replication_edges should be an array of symbols" unless replication_edges.is_a?(Array) && replication_edges.all? { |m| m.is_a?(Symbol) }
        raise "replication_edges should only be set when traversable?" unless inverse.traversable?
        raise "replication_edges should only contain valid graphs" unless replication_edges.all? { |m| inverse.attribute.entity.data_module.repository.imit.graph_by_name(m) }
        @replication_edges = replication_edges
      end

      def replication_edges
        @replication_edges || []
      end
    end

    class ImitationAttribute < Domgen.ParentedElement(:attribute)

      def client_side?
        !attribute.reference? || attribute.referenced_entity.imit?
      end

      def filter_in_graphs=(filter_in_graphs)
        raise "filter_in_graphs should be an array of symbols" unless filter_in_graphs.is_a?(Array) && filter_in_graphs.all? { |m| m.is_a?(Symbol) }
        raise "filter_in_graphs should only contain valid graphs" unless filter_in_graphs.all? { |m| attribute.entity.data_module.repository.imit.graph_by_name(m) }
        @filter_in_graphs = filter_in_graphs
      end

      def filter_in_graphs
        @filter_in_graphs || []
      end

      # TODO: Remove this ugly hack as soon as we can calculate whether an entitiy
      # is shared between multiple graph instances
      def traverse_during_unload?
        @traverse_during_unload.nil? ? true : @traverse_during_unload
      end

      def traverse_during_unload=(traverse_during_unload)
        @traverse_during_unload = !!traverse_during_unload
      end

      def include_edges
        @include_edges ||= []
      end

      def include_edges=(include_edges)
        @include_edges = include_edges
      end

      include Domgen::Java::ImitJavaCharacteristic

      protected

      def characteristic
        attribute
      end
    end

    class ImitationResult < Domgen.ParentedElement(:result)

      include Domgen::Java::ImitJavaCharacteristic

      protected

      def characteristic
        result
      end
    end

    class ImitationParameter < Domgen.ParentedElement(:parameter)
      include Domgen::Java::ImitJavaCharacteristic

      def environmental?
        parameter.gwt_rpc? && parameter.gwt_rpc.environmental?
      end

      protected

      def characteristic
        parameter
      end
    end

    class ImitationService < Domgen.ParentedElement(:service)
      attr_writer :name

      def name
        @name || service.name
      end

      def qualified_name
        "#{service.data_module.imit.client_service_package}.#{name}"
      end

      attr_writer :proxy_name

      def proxy_name
        @proxy_name || "#{name}Proxy"
      end

      def qualified_proxy_name
        "#{service.data_module.imit.client_service_package}.#{proxy_name}"
      end
    end

    class ImitationMethod < Domgen.ParentedElement(:method)

      def bulk_load=(bulk_load)
        @bulk_load = !!bulk_load
      end

      def bulk_load?
        @bulk_load.nil? ? false : @bulk_load
      end

      # TODO: Remove this ugly hack!
      attr_accessor :graph_to_subscribe
    end

    class ImitationException < Domgen.ParentedElement(:exception)
      def name
        exception.name.to_s =~ /Exception$/ ? exception.name.to_s : "#{exception.name}Exception"
      end

      def qualified_name
        "#{exception.data_module.imit.client_service_package}.#{name}"
      end
    end

    class ImitationEntity < Domgen.ParentedElement(:entity)

      def transport_id
        raise "Attempted to invoke transport_id on abstract entity" if entity.abstract?
        @transport_id
      end

      def transport_id=(transport_id)
        raise "Attempted to assign transport_id on abstract entity" if entity.abstract?
        @transport_id = transport_id
      end

      def name
        entity.name
      end

      def qualified_name
        "#{entity.data_module.imit.client_entity_package}.#{name}"
      end

      def replication_root?
        entity.data_module.repository.imit.graphs.any?{|g| g.instance_root? && g.instance_root.to_s == entity.qualified_name.to_s }
      end

      def associated_instance_root_graphs
        entity.data_module.repository.imit.graphs.select {|g| g.instance_root? && g.instance_root.to_s == entity.qualified_name.to_s }
      end

      def associated_type_graphs
        entity.data_module.repository.imit.graphs.select {|g| !g.instance_root? && g.type_roots.include?(entity.qualified_name.to_s) }
      end

      def replicate(graph, replication_type)
        raise "#{replication_type.inspect} is not of a known type" unless [:instance, :type].include?(replication_type)
        graph = entity.data_module.repository.imit.graph_by_name(graph)
        k = entity.qualified_name
        graph.instance_root = k if :instance == replication_type
        graph.type_roots.concat([k.to_s]) if :type == replication_type
      end

      def replication_graphs
        entity.data_module.repository.imit.graphs.select do |graph|
          (graph.instance_root? && graph.reachable_entities.include?(entity.qualified_name.to_s)) ||
            (!graph.instance_root? && graph.type_roots.include?(entity.qualified_name.to_s)) ||
            entity.attributes.any?{|a| a.imit? && a.imit.filter_in_graphs.include?(graph.name)}
        end
      end

      def referencing_client_side_attributes
        entity.referencing_attributes.select do |attribute|
          attribute.entity.imit? &&
            attribute.inverse.imit? &&
            attribute.inverse.imit.traversable? &&
            entity == attribute.referenced_entity &&
            attribute.imit? &&
            attribute.referenced_entity.imit?
        end
      end

      def post_verify
        entity.jpa.entity_listeners << entity.data_module.repository.imit.qualified_change_recorder_name if entity.jpa?
      end
    end

    class ImitationModule < Domgen.ParentedElement(:data_module)
      include Domgen::Java::ImitJavaPackage

      attr_writer :server_comm_package

      def server_comm_package
        @server_comm_package || resolve_package(:server_comm_package)
      end

      attr_writer :client_comm_package

      def client_comm_package
        @client_comm_package || resolve_package(:client_comm_package)
      end

      def mapper_name
        "#{data_module.name}Mapper"
      end

      def qualified_mapper_name
        "#{client_entity_package}.#{mapper_name}"
      end
    end

    class ReplicationGraph < Domgen.ParentedElement(:application)
      def initialize(application, name, options, &block)
        @name = name
        @type_roots = []
        @instance_root = nil
        application.send :register_graph, name, self
        super(application, options, &block)
      end

      attr_reader :application

      attr_reader :name

      def qualified_name
        "#{application.repository.qualified_name}.Graphs.#{name}"
      end

      def cacheable?
        !!@cacheable
      end

      def cacheable=(cacheable)
        @cacheable = cacheable
      end

      def instance_root?
        !@instance_root.nil?
      end

      def type_roots
        raise "type_roots invoked for graph #{name} when instance based" if instance_root?
        @type_roots
      end

      def type_roots=(type_roots)
        raise "Attempted to assign type_roots #{type_roots.inspect} for graph #{name} when instance based on #{@instance_root.inspect}" if instance_root?
        @type_roots = type_roots
      end

      def instance_root
        raise "instance_root invoked for graph #{name} when not instance based" if 0 != @type_roots.size
        @instance_root
      end

      def instance_root=(instance_root)
        raise "Attempted to assign instance_root to #{instance_root.inspect} for graph #{name} when not instance based (type_roots=#{@type_roots.inspect})" if 0 != @type_roots.size
        @instance_root = instance_root
      end

      # Return the list of entities reachable in instance graph
      def reachable_entities
        raise "reachable_entities invoked for graph #{name} when not instance based" if 0 != @type_roots.size
        @reachable_entities ||= []
      end

      def filter(parameter_type, options = {}, &block)
        Domgen.error("Attempting to redefine filter on graph #{self.name}") if @filter
        @filter ||= FilterParameter.new(self, parameter_type, options, &block)
      end

      def filter_parameter
        @filter
      end

      def post_verify
        if cacheable? && (filter_parameter || instance_root?)
          raise "Cacheable graphs are not supported for instance based or filterable graphs"
        end
      end
    end

    class FilterParameter < Domgen.ParentedElement(:graph)
      attr_reader :filter_type

      include Characteristic

      def initialize(graph, filter_type, options, &block)
        @filter_type = filter_type
        super(graph, options, &block)
      end

      def name
        "FilterParameter"
      end

      def qualified_name
        "#{graph.qualified_name}$#{name}"
      end

      def to_s
        "FilterParameter[#{self.qualified_name}]"
      end

      def characteristic_type_key
        filter_type
      end

      def characteristic_container
        graph
      end

      def struct_by_name(name)
        self.graph.application.repository.struct_by_name(name)
      end

      def entity_by_name(name)
        self.graph.application.repository.entity_by_name(name)
      end
    end

    class ImitationApplication < Domgen.ParentedElement(:repository)
      include Domgen::Java::JavaClientServerApplication

      attr_writer :async_callback_name

      def async_callback_name
        @async_callback_name || "#{repository.name}AsyncCallback"
      end

      def qualified_async_callback_name
        "#{client_service_package}.#{async_callback_name}"
      end

      attr_writer :async_error_callback_name

      def async_error_callback_name
        @async_error_callback_name || "#{repository.name}AsyncErrorCallback"
      end

      def qualified_async_error_callback_name
        "#{client_service_package}.#{async_error_callback_name}"
      end

      def client_ioc_package
        repository.gwt_rpc.client_ioc_package
      end

      attr_writer :server_comm_package

      def server_comm_package
        @server_comm_package || "#{server_package}.net"
      end

      attr_writer :client_comm_package

      def client_comm_package
        @client_comm_package || "#{client_package}.net"
      end

      def change_mapper_name
        "#{repository.name}ChangeMapper"
      end

      def qualified_change_mapper_name
        "#{client_comm_package}.#{change_mapper_name}"
      end

      def data_loader_service_name
        "Abstract#{repository.name}DataLoaderService"
      end

      def qualified_data_loader_service_name
        "#{client_comm_package}.#{data_loader_service_name}"
      end

      def client_session_name
        "#{repository.name}ClientSessionImpl"
      end

      def qualified_client_session_name
        "#{client_comm_package}.#{client_session_name}"
      end

      def client_session_interface_name
        "#{repository.name}ClientSession"
      end

      def qualified_client_session_interface_name
        "#{client_comm_package}.#{client_session_interface_name}"
      end

      def graph_enum_name
        "#{repository.name}ReplicationGraph"
      end

      def qualified_graph_enum_name
        "#{shared_entity_package}.#{graph_enum_name}"
      end

      def session_name
        "#{repository.name}Session"
      end

      def qualified_session_name
        "#{server_comm_package}.#{session_name}"
      end

      def session_manager_name
        "Abstract#{repository.name}SessionManager"
      end

      def qualified_session_manager_name
        "#{server_comm_package}.#{session_manager_name}"
      end

      def server_session_context_name
        "#{repository.name}SessionContext"
      end

      def qualified_server_session_context_name
        "#{server_comm_package}.#{server_session_context_name}"
      end

      def router_interface_name
        "#{repository.name}Router"
      end

      def qualified_router_interface_name
        "#{server_comm_package}.#{router_interface_name}"
      end

      def router_impl_name
        "#{repository.name}RouterImpl"
      end

      def qualified_router_impl_name
        "#{server_comm_package}.#{router_impl_name}"
      end

      def jpa_encoder_name
        "#{repository.name}JpaEncoder"
      end

      def qualified_jpa_encoder_name
        "#{server_comm_package}.#{jpa_encoder_name}"
      end

      def message_constants_name
        "#{repository.name}MessageConstants"
      end

      def qualified_message_constants_name
        "#{server_comm_package}.#{message_constants_name}"
      end

      def message_generator_name
        "#{repository.name}EntityMessageGenerator"
      end

      def qualified_message_generator_name
        "#{server_comm_package}.#{message_generator_name}"
      end

      def graph_encoder_name
        "#{repository.name}GraphEncoder"
      end

      def qualified_graph_encoder_name
        "#{server_comm_package}.#{graph_encoder_name}"
      end

      def change_recorder_name
        "#{repository.name}ChangeRecorder"
      end

      def qualified_change_recorder_name
        "#{server_comm_package}.#{change_recorder_name}"
      end

      def replication_interceptor_name
        "#{repository.name}ReplicationInterceptor"
      end

      def qualified_replication_interceptor_name
        "#{server_comm_package}.#{replication_interceptor_name}"
      end

      def graph_encoder_impl_name
        "#{repository.name}GraphEncoderImpl"
      end

      def qualified_graph_encoder_impl_name
        "#{server_comm_package}.#{graph_encoder_impl_name}"
      end

      attr_writer :services_module_name

      def services_module_name
        @services_module_name || "#{repository.name}ImitServicesModule"
      end

      def qualified_services_module_name
        "#{client_ioc_package}.#{services_module_name}"
      end

      attr_writer :mock_services_module_name

      def mock_services_module_name
        @mock_services_module_name || "#{repository.name}MockImitServicesModule"
      end

      def qualified_mock_services_module_name
        "#{client_ioc_package}.#{mock_services_module_name}"
      end

      def graphs
        graph_map.values
      end

      def graph(name, options = {}, &block)
        Domgen::Imit::ReplicationGraph.new(self, name, options, &block)
      end

      def graph_by_name(name)
        graph = graph_map[name.to_s]
        Domgen.error("Unable to locate graph #{name}") unless graph
        graph
      end

      def post_verify
        index = 0
        repository.data_modules.select { |data_module| data_module.imit? }.each do |data_module|
          data_module.entities.each do |entity|
            if entity.imit? && !entity.abstract?
              entity.imit.transport_id = index
              index += 1
            end
          end
        end
        repository.imit.graphs.select { |graph| graph.instance_root? }.each do |graph|
          entity_list = [repository.entity_by_name(graph.instance_root)]
          while entity_list.size > 0
            entity = entity_list.pop
            if !graph.reachable_entities.include?(entity.qualified_name.to_s)
              graph.reachable_entities << entity.qualified_name.to_s
              entity.referencing_attributes.each do |a|
                if a.imit? && a.imit.client_side? && a.inverse.imit.traversable? && !a.inverse.imit.exclude_edges.include?(graph.name)
                  a.inverse.imit.replication_edges = a.inverse.imit.replication_edges + [graph.name]
                  entity_list << a.entity unless graph.reachable_entities.include?(a.entity.qualified_name.to_s)
                end
              end
              entity.attributes.each do |a|
                if a.reference? && a.imit? && a.imit.client_side? && a.referenced_entity.imit? && a.imit.include_edges.include?(graph.name)
                  entity_list << a.referenced_entity unless graph.reachable_entities.include?(a.referenced_entity.qualified_name.to_s)
                end
              end
            end
          end
        end
        repository.imit.graphs.each {|g| g.post_verify}
      end

      private

      def register_graph(name, graph)
        graph_map[name.to_s] = graph
      end

      def graph_map
        @graphs ||= Domgen::OrderedHash.new
      end
    end
  end

  FacetManager.define_facet(:imit,
                            {
                              Attribute => Domgen::Imit::ImitationAttribute,
                              InverseElement => Domgen::Imit::ImitationAttributeInverse,
                              Entity => Domgen::Imit::ImitationEntity,
                              Method => Domgen::Imit::ImitationMethod,
                              Result => Domgen::Imit::ImitationResult,
                              Parameter => Domgen::Imit::ImitationParameter,
                              Exception => Domgen::Imit::ImitationException,
                              Service => Domgen::Imit::ImitationService,
                              DataModule => Domgen::Imit::ImitationModule,
                              Repository => Domgen::Imit::ImitationApplication
                            },
                            [:gwt_rpc])
end
