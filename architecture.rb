Domgen.repository(:Tyrell) do |repository|
  repository.enable_facets([:jpa, :mssql, :ejb, :jaxrs, :imit, :gwt_cache_filter])

  repository.gwt_rpc.module_name = 'example'

  repository.java.base_package = 'org.realityforge.tyrell'

  repository.ee.web_xml_fragments << 'server/src/main/etc/web.fragment.xml'

  repository.imit.graph(:MetaData, :cacheable => true)
  repository.imit.graph(:ShiftList, :require_type_graphs => [:MetaData])
  repository.imit.graph(:Shift, :require_type_graphs => [:MetaData])
  repository.imit.graph(:People, :require_type_graphs => [:MetaData])
  repository.imit.graph(:Person, :require_type_graphs => [:MetaData])
  repository.imit.graph(:PersonDetails, :require_type_graphs => [:MetaData])

  repository.gwt.include_standard_ux_test_module = false
  repository.gwt.include_standard_test_module = false
  repository.imit.include_standard_integration_test_module = false
  repository.ejb.include_server_test_module = false

  repository.data_module(:Tyrell) do |data_module|
    data_module.struct(:RosterSubscriptionDTO) do |s|
      s.date(:StartOn)
      s.integer(:NumberOfDays)

      s.imit.filter_for_graph(:ShiftList, :immutable => false)
    end

    data_module.entity(:RosterType) do |t|
      t.integer(:ID, :primary_key => true)
      t.string(:Code, 20)
      t.imit.replicate(:MetaData, :type)
    end

    data_module.entity(:Roster) do |t|
      t.integer(:ID, :primary_key => true)
      t.reference(:RosterType, :immutable => true)
      t.string(:Name, 100)
      t.imit.replicate(:MetaData, :type)
      t.imit.replicate(:ShiftList, :instance)
    end

    data_module.entity(:Shift) do |t|
      t.integer(:ID, :primary_key => true)
      t.reference(:Roster, :immutable => true, 'inverse.traversable' => true)
      t.string(:Name, 50)
      t.datetime(:StartAt, :immutable => true, 'imit.filter_in_graphs' => [:ShiftList])
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
      t.reference(:Shift, :immutable => true, 'inverse.traversable' => true, 'inverse.imit.exclude_edges' => [:ShiftList])
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
      t.reference(:Person, :immutable => true, 'inverse.traversable' => true) do |a|
        a.imit.graph_link(:Shift, :Person)
        a.inverse.imit.exclude_edges << :PersonDetails
      end
      t.reference(:Position, :immutable => true, 'inverse.traversable' => true)
    end

    data_module.entity(:Contact) do |t|
      t.integer(:ID, :primary_key => true)
      t.reference(:Person, :immutable => true, 'inverse.traversable' => true, 'inverse.imit.exclude_edges' => [:Person])
      t.string(:Email, 50)
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
  end

  repository.data_modules.each do |data_module|
    data_module.daos.each do |dao|
      unless %w(PersonRepository RosterRepository RosterTypeRepository).include?(dao.name.to_s)
        dao.disable_facet(:imit) if dao.imit?
      end
    end
    data_module.services.each do |service|
      service.disable_facet(:jaxrs) if service.jaxrs?
    end
  end
end
