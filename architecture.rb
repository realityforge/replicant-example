Domgen.repository(:Tyrell) do |repository|
  repository.enable_facet(:jpa)
  repository.enable_facet(:jackson)
  repository.enable_facet(:ejb)
  repository.enable_facet(:xml)
  repository.enable_facet(:imit)

  repository.jpa.provider = :eclipselink

  repository.gwt.base_package =
    repository.gwt_rpc.base_package =
      repository.imit.base_package =
        repository.jpa.base_package =
          repository.ee.base_package =
            repository.ejb.base_package = 'org.realityforge.replicant.example'


  repository.data_module(:Tyrell) do |data_module|

    data_module.entity(:Building) do |t|
      t.integer(:ID, :primary_key => true)
      t.string(:Name, 100)
    end

    data_module.entity(:Room) do |t|
      t.integer(:ID, :primary_key => true)
      t.reference(:Building, :immutable => true, :"inverse.traversable" => true)
      t.integer(:Floor, :immutable => true)
      t.integer(:LocalNumber)
      t.string(:Name, 50)
      t.boolean(:Active)
    end

    data_module.message(:BuildingDataLoaded)

    data_module.message(:IncrementalLoadComplete)

    data_module.message(:BulkLoadComplete)

    data_module.message(:SystemError) do |m|
      m.text(:Message)
      m.parameter(:Throwable, "java.lang.Throwable", :nullable => true)
    end

    data_module.service(:BuildingService) do |s|
      s.method(:CreateBuilding) do |m|
        m.text(:Name)
        m.returns(:reference, :referenced_entity => :Building)
      end
      s.method(:RemoveBuilding) do |m|
        m.reference(:Building)
      end
      s.method(:SetBuildingName) do |m|
        m.reference(:Building)
        m.text(:Name)
      end
      s.method(:CreateRoom) do |m|
        m.reference(:Building)
        m.integer(:Floor)
        m.integer(:LocalNumber)
        m.text(:Name)
        m.boolean(:Active)
        m.returns(:reference, :referenced_entity => :Room)
      end
      s.method(:RemoveRoom) do |m|
        m.reference(:Room)
      end
      s.method(:SetRoomName) do |m|
        m.reference(:Room)
        m.text(:Name)
      end
      s.method(:SetRoomLocalNumber) do |m|
        m.reference(:Room)
        m.integer(:LocalNumber)
      end
      s.method(:SetRoomActivity) do |m|
        m.reference(:Room)
        m.boolean(:Active)
      end
    end

    data_module.exception(:BadSession, "ejb.rollback" => false)

    data_module.service(:SubscriptionService) do |s|
      s.method(:SubscribeToBuilding) do |m|
        m.string(:ClientID, 50, :"gwt_rpc.environment_key" => "request:cookie:sid")
        m.reference(:Building)
        m.returns(:text)
        m.exception(:BadSession)
      end
      s.method(:UnsubscribeFromBuilding) do |m|
        m.string(:ClientID, 50, :"gwt_rpc.environment_key" => "request:cookie:sid")
        m.reference(:Building)
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
  end
end
