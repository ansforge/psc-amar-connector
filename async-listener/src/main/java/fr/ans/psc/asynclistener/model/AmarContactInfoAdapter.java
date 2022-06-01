package fr.ans.psc.asynclistener.model;

import fr.ans.psc.amar.model.ContactInfo;
import fr.ans.psc.model.Ps;

public class AmarContactInfoAdapter extends ContactInfo {

    public AmarContactInfoAdapter(Ps ps) {
        setEmail(ps.getEmail());
        setPhone(ps.getPhone());
    }
}
