package fr.ans.psc.asynclistener.model;

import fr.ans.psc.amar.model.Activity;
import fr.ans.psc.model.WorkSituation;

public class AmarActivityAdapter extends Activity {

    public AmarActivityAdapter(WorkSituation workSituation) {
        setActivitySectorCode(workSituation.getActivitySectorCode());
    }
}
