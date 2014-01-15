Domgen.repository(:Tyrell) do |repository|
  repository.enable_facet(:jpa)
  repository.enable_facet(:jackson)
  repository.enable_facet(:ejb)
  repository.enable_facet(:xml)
  repository.enable_facet(:imit)

  repository.jpa.provider = :eclipselink

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
      s.method(:SetBuildingName) do |m|
        m.reference(:Building)
        m.text(:Name)
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

    data_module.service(:SubscriptionService) do |s|
      s.method(:SubscribeToBuilding) do |m|
        m.reference(:Building)
      end
      s.method(:UnsubscribeFromBuilding) do |m|
        m.reference(:Building)
      end
    end
  end
end
