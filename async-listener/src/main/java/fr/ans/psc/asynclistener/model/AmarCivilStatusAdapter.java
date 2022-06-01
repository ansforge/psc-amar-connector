package fr.ans.psc.asynclistener.model;

import fr.ans.psc.amar.model.CivilStatus;
import fr.ans.psc.model.Ps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AmarCivilStatusAdapter extends CivilStatus {

    public AmarCivilStatusAdapter(Ps ps) {
        setLastName(ps.getLastName());
        String[] amarFirstNamesArray = ps.getFirstName().split("/");
        List<String> amarFirstNames = Arrays.stream(amarFirstNamesArray).collect(Collectors.toList());
        setFirstNames(amarFirstNames);
        setBirthdate(ps.getDateOfBirth());
        setBirthplace(ps.getBirthAddress());
        setBirthCountryCode(ps.getBirthCountryCode());
        setBirthTownCode(ps.getBirthAddressCode());
        setGenderCode(ps.getGenderCode());
        setPersonalCivilityTitle(ps.getSalutationCode());
    }
}
