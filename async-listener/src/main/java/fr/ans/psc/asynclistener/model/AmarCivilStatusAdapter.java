package fr.ans.psc.asynclistener.model;

import fr.ans.psc.amar.model.CivilStatus;
import fr.ans.psc.model.FirstName;
import fr.ans.psc.model.Ps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AmarCivilStatusAdapter extends CivilStatus {

    public AmarCivilStatusAdapter(Ps ps) {
        setLastName(ps.getLastName());
        setFirstNames(extractNames(ps.getFirstNames()));
        setBirthdate(ps.getDateOfBirth());
        setBirthplace(ps.getBirthAddress());
        setBirthCountryCode(ps.getBirthCountryCode());
        setBirthTownCode(ps.getBirthAddressCode());
        setGenderCode(ps.getGenderCode());
        setPersonalCivilityTitle(ps.getSalutationCode());
    }

    private List<String> extractNames(List<FirstName> firstNames) {
        return firstNames.stream().sorted(this::compareFirstNames).map(FirstName::getFirstName).collect(Collectors.toList());
    }

    private int compareFirstNames(FirstName fn1, FirstName fn2) {
        return fn1.getOrder().compareTo(fn2.getOrder());
    }
}
