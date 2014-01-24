package org.realityforge.replicant.example.server.service;

import javax.annotation.Nonnull;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.realityforge.replicant.example.server.entity.Roster;
import org.realityforge.replicant.example.server.entity.Shift;
import org.realityforge.replicant.example.server.entity.dao.RosterRepository;
import org.realityforge.replicant.example.server.entity.dao.ShiftRepository;

@Stateless( name = RosterService.EJB_NAME )
public class RosterServiceEJB
  implements RosterService
{
  @EJB
  private RosterRepository _rosterRepository;

  @EJB
  private ShiftRepository _shiftRepository;

  @Override
  @Nonnull
  public Roster createRoster( @Nonnull final String name )
  {
    final Roster roster = new Roster();
    roster.setName( name );
    _rosterRepository.persist( roster );
    return roster;
  }

  @Override
  public void removeRoster( @Nonnull final Roster roster )
  {
    for ( final Shift shift : roster.getShifts() )
    {
      removeShift( shift );
    }
    _rosterRepository.remove( roster );
  }

  @Override
  public void setRosterName( @Nonnull final Roster roster, @Nonnull final String name )
  {
    roster.setName( name );
  }

  @Override
  @Nonnull
  public Shift createShift( @Nonnull final Roster roster, @Nonnull final String name )
  {
    final Shift shift = new Shift( roster );
    shift.setName( name );
    _shiftRepository.persist( shift );
    return shift;
  }

  @Override
  public void removeShift( @Nonnull final Shift shift )
  {
    _shiftRepository.remove( shift );
  }

  @Override
  public void setShiftName( @Nonnull final Shift shift, @Nonnull final String name )
  {
    shift.setName( name );
  }
}
