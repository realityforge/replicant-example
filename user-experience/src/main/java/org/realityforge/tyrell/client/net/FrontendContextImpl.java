package org.realityforge.tyrell.client.net;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.realityforge.gwt.datatypes.client.date.RDate;
import org.realityforge.replicant.client.EntityRepository;
import org.realityforge.replicant.client.EntitySubscriptionManager;
import org.realityforge.replicant.client.net.gwt.AbstractFrontendContextImpl;
import org.realityforge.replicant.client.runtime.AreaOfInterestService;
import org.realityforge.replicant.client.runtime.ContextConverger;
import org.realityforge.replicant.client.runtime.ReplicantClientSystem;
import org.realityforge.replicant.client.runtime.Scope;
import org.realityforge.replicant.client.runtime.ScopeReference;
import org.realityforge.tyrell.client.data_type.RosterSubscriptionDTO;
import org.realityforge.tyrell.client.data_type.RosterSubscriptionDTOFactory;
import org.realityforge.tyrell.client.entity.Roster;
import org.realityforge.tyrell.client.entity.Shift;

@SuppressWarnings( { "Duplicates", "CdiInjectionPointsInspection" } )
public class FrontendContextImpl
  extends AbstractFrontendContextImpl
  implements TyrellGwtRuntimeExtension, FrontendContext
{
  private static final int NUMBER_OF_DAYS = 7;
  private static final String META_DATA_SCOPE = "MetaData";
  private static final String ROSTER_SCOPE_NAME = "ShiftList";
  private static final String SHIFT_SCOPE_NAME = "Shift";
  private static final String PEOPLE_SCOPE_NAME = "People";

  private ScopeReference _shiftScope;
  private ScopeReference _rosterScope;
  private ScopeReference _peopleScope;

  @Nullable
  private Shift _currentShift;
  @Nullable
  private Roster _currentRoster;
  @Nullable
  private RDate _currentRosterStartOn;
  private boolean _loadPeople;

  @Inject
  public FrontendContextImpl( @Nonnull final ContextConverger converger,
                              @Nonnull final EntityRepository repository,
                              @Nonnull final ReplicantClientSystem replicantClientSystem,
                              @Nonnull final AreaOfInterestService areaOfInterestService,
                              @Nonnull final EntitySubscriptionManager entitySubscriptionManager )
  {
    super( converger, repository, entitySubscriptionManager, replicantClientSystem, areaOfInterestService );
  }

  @Override
  public void loadPeople()
  {
    _loadPeople = true;
  }

  @Override
  public void unloadPeople()
  {
    _loadPeople = false;
  }

  @Override
  public void selectShift( @Nullable final Shift shift )
  {
    _currentShift = shift;
  }

  @Override
  public void selectRoster( @Nullable final Roster roster, @Nullable final RDate startOn )
  {
    _currentRoster = roster;
    _currentRosterStartOn = startOn;
  }

  @Override
  protected void preConverge()
  {
    convergeSubscriptions();
  }

  @Override
  protected void initialSubscriptionSetup()
  {
    final Scope scope = getAreaOfInterestService().createScopeReference( META_DATA_SCOPE ).getScope();
    subscribeToTyrellMetaDataGraph( scope );
  }

  private void convergeSubscriptions()
  {
    if ( null == _currentRoster )
    {
      if ( null != _rosterScope )
      {
        _rosterScope.release();
        _rosterScope = null;
      }
    }
    else
    {
      if ( null == _rosterScope || _rosterScope.hasBeenReleased() )
      {
        _rosterScope = getAreaOfInterestService().createScopeReference( ROSTER_SCOPE_NAME );
      }
      assert null != _currentRosterStartOn;
      final RosterSubscriptionDTO filter = RosterSubscriptionDTOFactory.create( _currentRosterStartOn, NUMBER_OF_DAYS );
      subscribeToTyrellShiftListGraph( _rosterScope.getScope(), _currentRoster.getID(), filter );
    }

    if ( null == _currentShift )
    {
      if ( null != _shiftScope )
      {
        _shiftScope.release();
        _shiftScope = null;
      }
    }
    else
    {
      if ( null == _shiftScope || _shiftScope.hasBeenReleased() )
      {
        _shiftScope = getAreaOfInterestService().createScopeReference( SHIFT_SCOPE_NAME );
      }
      subscribeToTyrellShiftGraph( _shiftScope.getScope(), _currentShift.getID() );
    }

    if ( _loadPeople )
    {
      if ( null == _peopleScope || _peopleScope.hasBeenReleased() )
      {
        _peopleScope = getAreaOfInterestService().createScopeReference( PEOPLE_SCOPE_NAME );
      }
      subscribeToTyrellPeopleGraph( _peopleScope.getScope() );
    }
    else
    {
      if ( null != _peopleScope )
      {
        _peopleScope.release();
        _peopleScope = null;
      }
    }
  }
}
