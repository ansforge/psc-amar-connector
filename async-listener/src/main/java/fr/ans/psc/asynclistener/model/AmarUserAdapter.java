package fr.ans.psc.asynclistener.model;

import fr.ans.psc.amar.model.*;
import fr.ans.psc.model.Ps;

import java.util.ArrayList;
import java.util.List;

public class AmarUserAdapter extends User {

    private final int DEFAULT_QUALITY = 1;

    public AmarUserAdapter(Ps ps) {
        ContactInfo contactInfo = new AmarContactInfoAdapter(ps);
        CivilStatus civilStatus = new AmarCivilStatusAdapter(ps);
        List<Practice> practices = new ArrayList<>();
        ps.getProfessions().forEach(profession -> practices.add(new AmarPracticeAdapter(profession)));
        List<AlternativeIdentifier> alternativeIdentifiers = new ArrayList<>();
        ps.getIds().forEach(id ->
        {
            AlternativeIdentifier identifier = new AlternativeIdentifier();
            identifier.setIdentifier(id);
            identifier.setOrigine(getOriginFromId(id));
            identifier.setQuality(DEFAULT_QUALITY);
            alternativeIdentifiers.add(identifier);
        });

        setNationalId(ps.getNationalId());
        setContactInfo(contactInfo);
        setCivilStatus(civilStatus);
        setPractices(practices);
        setAlternativeIdentifiers(alternativeIdentifiers);
    }

    private String getOriginFromId(String id) {
        switch (id.charAt(0)) {
            case ('0'):
                return "ADELI";
            case ('3'):
                return "FINESS";
            case ('4'):
                return "SIREN";
            case ('5'):
                return "SIRET";
            case ('8'):
                return "RPPS";
            default:
                return "";
        }
    }
}