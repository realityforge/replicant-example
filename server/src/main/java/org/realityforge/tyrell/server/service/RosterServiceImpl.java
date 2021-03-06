package org.realityforge.tyrell.server.service;

import java.util.ArrayList;
import java.util.Date;
import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.realityforge.tyrell.server.entity.Assignment;
import org.realityforge.tyrell.server.entity.Contact;
import org.realityforge.tyrell.server.entity.Person;
import org.realityforge.tyrell.server.entity.Position;
import org.realityforge.tyrell.server.entity.Roster;
import org.realityforge.tyrell.server.entity.RosterType;
import org.realityforge.tyrell.server.entity.Shift;
import org.realityforge.tyrell.server.entity.dao.AssignmentRepository;
import org.realityforge.tyrell.server.entity.dao.ContactRepository;
import org.realityforge.tyrell.server.entity.dao.PersonRepository;
import org.realityforge.tyrell.server.entity.dao.PositionRepository;
import org.realityforge.tyrell.server.entity.dao.RosterRepository;
import org.realityforge.tyrell.server.entity.dao.ShiftRepository;

@ApplicationScoped
@Transactional( Transactional.TxType.REQUIRED )
@Typed( RosterService.class )
public class RosterServiceImpl
  implements RosterService
{
  private static final String[] FIRST_NAMES = new String[]{
    "Hollie", "Emerson", "Healy", "Brigitte", "Elba", "Claudio",
    "Dena", "Christina", "Gail", "Orville", "Rae", "Mildred",
    "Candice", "Louise", "Emilio", "Geneva", "Heriberto", "Bulrush",
    "Abigail", "Chad", "Terry", "Bell" };

  private static final String[] LAST_NAMES = new String[]{
    "Voss", "Milton", "Colette", "Cobb", "Lockhart", "Engle",
    "Pacheco", "Blake", "Horton", "Daniel", "Childers", "Starnes",
    "Carson", "Kelchner", "Hutchinson", "Underwood", "Rush", "Bouchard",
    "Louis", "Andrews", "English", "Snedden" };

  private static final String[] EMAILS = new String[]{
    "mark@example.com", "hollie@example.com", "boticario@example.com",
    "emerson@example.com", "healy@example.com", "brigitte@example.com",
    "elba@example.com", "claudio@example.com", "dena@example.com",
    "brasilsp@example.com", "parker@example.com", "derbvktqsr@example.com",
    "qetlyxxogg@example.com", "antenas_sul@example.com",
    "cblake@example.com", "gailh@example.com", "orville@example.com",
    "post_master@example.com", "rchilders@example.com", "buster@example.com",
    "user31065@example.com", "ftsgeolbx@example.com" };

  @Inject
  private RosterRepository _rosterRepository;
  @Inject
  private ShiftRepository _shiftRepository;
  @Inject
  private PositionRepository _positionRepository;
  @Inject
  private AssignmentRepository _assignmentRepository;
  @Inject
  private PersonRepository _personRepository;
  @Inject
  private ContactRepository _contactRepository;

  @PostConstruct
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  public void setupPersonDatabase()
  {
    initPeopleIfRequired();
  }

  private void initPeopleIfRequired()
  {
    if ( 0 == _personRepository.findAll().size() )
    {
      for ( int i = 0; i < FIRST_NAMES.length && i < LAST_NAMES.length && i < EMAILS.length; ++i )
      {
        final Person person = new Person();
        person.setName( LAST_NAMES[ i ] + ", " + FIRST_NAMES[ i ] );
        _personRepository.persist( person );

        final Contact contact = new Contact( person );
        contact.setEmail( EMAILS[ i ] );
        _contactRepository.persist( contact );
      }
    }
  }

  @Override
  @Nonnull
  public Roster createRoster( @Nonnull final RosterType rosterType, @Nonnull final String name )
  {
    final Roster roster = new Roster( rosterType );
    roster.setName( name );
    _rosterRepository.persist( roster );
    return roster;
  }

  @Override
  public void removeRoster( @Nonnull final Roster roster )
  {
    for ( final Shift shift : new ArrayList<>( roster.getShifts() ) )
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
  public Shift createShift( @Nonnull final Roster roster, @Nonnull final String name, @Nonnull final Date shiftOn )
  {
    final Shift shift = new Shift( roster, shiftOn );
    shift.setName( name );
    _shiftRepository.persist( shift );
    return shift;
  }

  @Override
  public void removeShift( @Nonnull final Shift shift )
  {
    for ( final Position position : new ArrayList<>( shift.getPositions() ) )
    {
      removePosition( position );
    }
    _shiftRepository.remove( shift );
  }

  @Override
  public void setShiftName( @Nonnull final Shift shift, @Nonnull final String name )
  {
    shift.setName( name );
  }

  @Override
  @Nonnull
  public Position createPosition( @Nonnull final Shift shift, @Nonnull final String name )
  {
    final Position position = new Position( shift );
    position.setName( name );
    _positionRepository.persist( position );
    return position;
  }

  @Override
  public void removePosition( @Nonnull final Position position )
  {
    removeAssignments( position );
    _positionRepository.remove( position );
  }

  @Override
  public void setPositionName( @Nonnull final Position position, @Nonnull final String name )
  {
    position.setName( name );
  }

  @Override
  public void assignPerson( @Nonnull final Position position, @Nonnull final Person person )
  {
    removeAssignments( position );
    _assignmentRepository.persist( new Assignment( person, position ) );
  }

  private void removeAssignments( final Position position )
  {
    for ( final Assignment assignment : new ArrayList<>( position.getAssignments() ) )
    {
      _assignmentRepository.remove( assignment );
    }
  }
}
