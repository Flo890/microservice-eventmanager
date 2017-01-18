package testutils;

/**
 * Created by Flo on 11/08/2016.
 */
public enum BikemapEventtype {

    new_trail_created,
    trail_deleted,
    trailride_moved_to_other_trail,
    generated_trail_heightprofile,
    new_trailride_created,
    new_trail_usermeta,
    updated_trail_usermeta,
    enriched_trail_osmmeta,
    new_trail_geometa,
    updated_trail_geometa,
    new_stravaactivity_imported,
    new_osmwayride_created,
    upserted_speedprofile_osmwaypart,
    upserted_speedprofile_trail,
    integration_test_event;

    public static BikemapEventtype fromString(String name){
        for(BikemapEventtype aBikemapEventtype : BikemapEventtype.values()){
            if(aBikemapEventtype.name().equals(name)){
                return aBikemapEventtype;
            }
        }
        return null;
    }
}
