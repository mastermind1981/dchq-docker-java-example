package dchq.dbconnect.service;

import dchq.dbconnect.model.NameDirectory;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 11/22/2015
 */
//@Service("NameDirectoryService")
public class NameDirectoryServiceMock implements NameDirectoryService {
    private static List<NameDirectory> rsList = new ArrayList<NameDirectory>();
    private static Long id = 0L;

    @Override
    public List<NameDirectory> getAllRows() {
        return rsList;
    }

    @Override
    public void addNameDirectory(NameDirectory nd) {
        nd.setId(++id);
        rsList.add(nd);
    }

    @Override
    public void deleteNameDirectoryById(Long id) {
        NameDirectory found = findRowById(id);
        if (found != null) {
            rsList.remove(found);
        }
    }

    @Override
    public void deleteAll() {
        rsList.clear();
        id = 0L;
    }

    private NameDirectory findRowById(Long id) {
        for (NameDirectory rs : rsList) {
            if (rs.getId().equals(id)) {
                return rs;
            }
        }

        return null;
    }
}
