Domgen.repository(:Tyrell) do |repository|
  repository.enable_facet(:jpa)
  repository.enable_facet(:jackson)
  repository.enable_facet(:ejb)
  repository.enable_facet(:xml)
  repository.enable_facet(:imit)

  repository.jpa.provider = :eclipselink

  repository.java.base_package = 'org.realityforge.replicant.example'

  repository.imit.graph(:MetaData, :cacheable => true)
  repository.imit.graph(:Roster)
  repository.imit.graph(:RosterList)

  repository.data_module(:Tyrell) do |data_module|

    data_module.entity(:RosterType) do |t|
      t.integer(:ID, :primary_key => true)
      t.string(:Code, 20)
      t.imit.replicate(:MetaData, :type)
    end

    data_module.entity(:Roster) do |t|
      t.integer(:ID, :primary_key => true)
      t.reference(:RosterType, :immutable => true)
      t.string(:Name, 100)
      t.imit.replicate(:Roster, :instance)
      t.imit.replicate(:RosterList, :type)
    end

    data_module.entity(:Shift) do |t|
      t.integer(:ID, :primary_key => true)
      t.reference(:Roster, :immutable => true, :"inverse.traversable" => true)
      t.string(:Name, 50)
    end

    data_module.entity(:Position) do |t|
      t.integer(:ID, :primary_key => true)
      t.reference(:Shift, :immutable => true, :"inverse.traversable" => true, "inverse.imit.exclude_edges" => [:RosterList])
      t.string(:Name, 50)
    end

    data_module.message(:SystemError) do |m|
      m.text(:Message)
      m.parameter(:Throwable, "java.lang.Throwable", :nullable => true)
    end

    data_module.service(:RosterService) do |s|
      s.method(:CreateRoster) do |m|
        m.reference(:RosterType)
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
        m.text(:ClientID)
        m.text(:ETag, :nullable => true)
        m.exception(:BadSession)
        m.returns(:boolean)
      end
      s.method(:UnsubscribeFromMetaData) do |m|
        m.string(:ClientID, 50)
        m.exception(:BadSession)
      end
      s.method(:DownloadAll) do |m|
        m.string(:ClientID, 50)
        m.exception(:BadSession)
      end
      s.method(:SubscribeToRoster) do |m|
        m.string(:ClientID, 50)
        m.reference(:Roster)
        m.exception(:BadSession)
      end
      s.method(:UnsubscribeFromRoster) do |m|
        m.string(:ClientID, 50)
        m.reference(:Roster)
        m.exception(:BadSession)
      end
      s.method(:SubscribeToRosterList) do |m|
        m.string(:ClientID, 50)
        m.exception(:BadSession)
      end
      s.method(:UnsubscribeFromRosterList) do |m|
        m.string(:ClientID, 50)
        m.exception(:BadSession)
      end
      s.method(:Poll) do |m|
        m.string(:ClientID, 50)
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
