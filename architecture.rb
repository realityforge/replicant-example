Domgen.repository(:Tyrell) do |repository|
  repository.enable_facet(:jpa)
  repository.enable_facet(:jackson)
  repository.enable_facet(:ejb)
  repository.enable_facet(:xml)
  repository.enable_facet(:imit)

  repository.jpa.provider = :eclipselink

  repository.java.base_package = 'org.realityforge.replicant.example'

  repository.data_module(:Tyrell) do |data_module|

    data_module.entity(:RosterType) do |t|
      t.integer(:ID, :primary_key => true)
      t.string(:Code, 20)
    end

    data_module.entity(:Roster) do |t|
      t.integer(:ID, :primary_key => true)
      t.string(:Name, 100)
      t.imit.replication_root = true
    end

    data_module.entity(:Shift) do |t|
      t.integer(:ID, :primary_key => true)
      t.reference(:Roster, :immutable => true, :"inverse.traversable" => true)
      t.string(:Name, 50)
    end

    data_module.message(:IncrementalLoadComplete)

    data_module.message(:BulkLoadComplete)

    data_module.message(:SystemError) do |m|
      m.text(:Message)
      m.parameter(:Throwable, "java.lang.Throwable", :nullable => true)
    end

    data_module.service(:RosterService) do |s|
      s.method(:CreateRoster) do |m|
        m.text(:Name)
        m.returns(:reference, :referenced_entity => :Roster)
      end
      s.method(:RemoveRoster) do |m|
        m.reference(:Roster)
      end
      s.method(:SetRosterName) do |m|
        m.reference(:Roster)
        m.text(:Name)
      end
      s.method(:CreateShift) do |m|
        m.reference(:Roster)
        m.text(:Name)
        m.returns(:reference, :referenced_entity => :Shift)
      end
      s.method(:RemoveShift) do |m|
        m.reference(:Shift)
      end
      s.method(:SetShiftName) do |m|
        m.reference(:Shift)
        m.text(:Name)
      end
    end

    data_module.exception(:BadSession, "ejb.rollback" => false)

    data_module.service(:SubscriptionService) do |s|
      s.method(:SubscribeToMetaData) do |m|
        m.string(:ClientID, 50, :"gwt_rpc.environment_key" => "request:cookie:sid")
        m.returns(:text)
        m.exception(:BadSession)
      end
      s.method(:UnsubscribeFromMetaData) do |m|
        m.string(:ClientID, 50, :"gwt_rpc.environment_key" => "request:cookie:sid")
        m.exception(:BadSession)
      end
      s.method(:DownloadAll) do |m|
        m.string(:ClientID, 50, :"gwt_rpc.environment_key" => "request:cookie:sid")
        m.returns(:text)
        m.exception(:BadSession)
      end
      s.method(:SubscribeToAll) do |m|
        m.string(:ClientID, 50, :"gwt_rpc.environment_key" => "request:cookie:sid")
        m.returns(:text)
        m.exception(:BadSession)
      end
      s.method(:SubscribeToRoster) do |m|
        m.string(:ClientID, 50, :"gwt_rpc.environment_key" => "request:cookie:sid")
        m.reference(:Roster)
        m.returns(:text)
        m.exception(:BadSession)
      end
      s.method(:UnsubscribeFromRoster) do |m|
        m.string(:ClientID, 50, :"gwt_rpc.environment_key" => "request:cookie:sid")
        m.reference(:Roster)
        m.exception(:BadSession)
      end
      s.method(:Poll) do |m|
        m.string(:ClientID, 50, :"gwt_rpc.environment_key" => "request:cookie:sid")
        m.integer(:LastSequenceAcked)
        m.returns(:text, :nullable => true) do |a|
          a.description("A changeset represented as json or null if no changeset outstanding.")
        end
        m.exception(:BadSession)
      end
    end

    data_module.services.each do |service|
      if service.ejb?
        service.ejb.generate_boundary = true
        service.ejb.boundary_extends = "org.realityforge.replicant.example.server.service.AbstractExternalService"
      end
    end
  end
end
