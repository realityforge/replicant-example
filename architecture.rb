Domgen.repository(:Tyrell) do |repository|
  repository.enable_facet(:jpa)
  repository.enable_facet(:jackson)
  repository.enable_facet(:ejb)
  repository.enable_facet(:xml)
  repository.enable_facet(:imit)

  repository.jpa.provider = :eclipselink

  repository.java.base_package = 'org.realityforge.replicant.example'

  repository.imit.graph(:MetaData, :cacheable => true)
  repository.imit.graph(:RosterList)
  repository.imit.graph(:ShiftList)
  repository.imit.graph(:Shift)
  repository.imit.graph(:People)
  repository.imit.graph(:Person)
  repository.imit.graph(:PersonDetails)
  repository.imit.invalid_session_exception = 'Tyrell.BadSession'

  repository.data_module(:Tyrell) do |data_module|
    data_module.struct(:RosterSubscriptionDTO) do |s|
      s.date(:StartOn)
      s.integer(:NumberOfDays)
    end
    repository.imit.graph_by_name(:ShiftList).filter(:struct, :referenced_struct => :"Tyrell.RosterSubscriptionDTO")

    data_module.entity(:RosterType) do |t|
      t.integer(:ID, :primary_key => true)
      t.string(:Code, 20)
      t.imit.replicate(:MetaData, :type)
    end

    data_module.entity(:Roster) do |t|
      t.integer(:ID, :primary_key => true)
      t.reference(:RosterType, :immutable => true)
      t.string(:Name, 100)
      t.imit.replicate(:RosterList, :type)
      t.imit.replicate(:ShiftList, :instance)
    end

    data_module.entity(:Shift) do |t|
      t.integer(:ID, :primary_key => true)
      t.reference(:Roster, :immutable => true, :"inverse.traversable" => true, "inverse.imit.exclude_edges" => [:RosterList])
      t.string(:Name, 50)
      t.datetime(:StartAt, :"imit.filter_in_graphs" => [:ShiftList])
      t.imit.replicate(:Shift, :instance)

      t.query(:FindAllByAreaOfInterest, 'jpa.jpql' => <<JPQL) do |q|
SELECT S
FROM
  Tyrell_Shift S
WHERE
  S.roster = :Roster AND
  S.startAt >= :From AND
  S.startAt < :To
JPQL
        q.reference(:Roster)
        q.datetime(:From)
        q.datetime(:To)
      end
    end

    data_module.entity(:Position) do |t|
      t.integer(:ID, :primary_key => true)
      t.reference(:Shift, :immutable => true, :"inverse.traversable" => true, "inverse.imit.exclude_edges" => [:ShiftList])
      t.string(:Name, 50)
    end

    data_module.entity(:Person) do |t|
      t.integer(:ID, :primary_key => true)
      t.string(:Name, 50)
      t.imit.replicate(:People, :type)
      t.imit.replicate(:Person, :instance)
      t.imit.replicate(:PersonDetails, :instance)
    end

    data_module.entity(:Assignment) do |t|
      t.integer(:ID, :primary_key => true)
      t.reference(:Person, :immutable => true,
                  "inverse.traversable" => true,
                  "imit.graph_links" => {:Shift => {:target_graph => :Person}},
                  "inverse.imit.exclude_edges" => [:Person, :PersonDetails])
      t.reference(:Position, :immutable => true, :"inverse.traversable" => true)
    end

    data_module.entity(:Contact) do |t|
      t.integer(:ID, :primary_key => true)
      t.reference(:Person, :immutable => true, :"inverse.traversable" => true, "inverse.imit.exclude_edges" => [:Person])
      t.string(:Email, 50)
    end

    data_module.message(:SessionEstablished)

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
        m.date(:ShiftOn)
        m.returns(:reference, :referenced_entity => :Shift)
      end
      s.method(:RemoveShift) do |m|
        m.reference(:Shift)
      end
      s.method(:SetShiftName) do |m|
        m.reference(:Shift)
        m.text(:Name)
      end
      s.method(:CreatePosition) do |m|
        m.reference(:Shift)
        m.text(:Name)
        m.returns(:reference, :referenced_entity => :Position)
      end
      s.method(:RemovePosition) do |m|
        m.reference(:Position)
      end
      s.method(:SetPositionName) do |m|
        m.reference(:Position)
        m.text(:Name)
      end
      s.method(:AssignPerson) do |m|
        m.reference(:Position)
        m.reference(:Person)
      end
    end

    data_module.exception(:BadSession, "ejb.rollback" => false)

    data_module.service(:SubscriptionService) do |s|
      s.method(:SubscribeToMetaData) do |m|
        m.imit.graph_to_subscribe = :MetaData
        m.text(:ClientID)
        m.text(:ETag, :nullable => true)
        m.exception(:BadSession)
        m.returns(:boolean)
      end
      s.method(:UnsubscribeFromMetaData) do |m|
        m.string(:ClientID, 50)
        m.exception(:BadSession)
      end
      s.method(:SubscribeToPeople) do |m|
        m.text(:ClientID)
        m.exception(:BadSession)
      end
      s.method(:UnsubscribeFromPeople) do |m|
        m.string(:ClientID, 50)
        m.exception(:BadSession)
      end
      s.method(:SubscribeToPersonDetails) do |m|
        m.text(:ClientID)
        m.reference(:Person)
        m.exception(:BadSession)
      end
      s.method(:UnsubscribeFromPersonDetails) do |m|
        m.string(:ClientID, 50)
        m.reference(:Person)
        m.exception(:BadSession)
      end
      s.method(:DownloadAll) do |m|
        m.string(:ClientID, 50)
        m.exception(:BadSession)
      end
      s.method(:SubscribeToShift) do |m|
        m.string(:ClientID, 50)
        m.reference(:Shift)
        m.exception(:BadSession)
      end
      s.method(:UnsubscribeFromShift) do |m|
        m.string(:ClientID, 50)
        m.reference(:Shift)
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
      s.method(:SubscribeToShiftList) do |m|
        m.string(:ClientID, 50)
        m.reference(:Roster)
        m.struct(:RosterSubscriptionDTO, :RosterSubscriptionDTO)
        m.exception(:BadSession)
      end
      s.method(:UpdateShiftListSubscription) do |m|
        m.string(:ClientID, 50)
        m.reference(:Roster)
        m.struct(:RosterSubscriptionDTO, :RosterSubscriptionDTO)
        m.exception(:BadSession)
      end
      s.method(:UnsubscribeFromShiftList) do |m|
        m.string(:ClientID, 50)
        m.reference(:Roster)
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
