package de.fau.cs.mad.fablab.android.model;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import de.fau.cs.mad.fablab.android.viewmodel.common.ObservableArrayList;
import de.fau.cs.mad.fablab.rest.core.ICal;
import de.fau.cs.mad.fablab.rest.myapi.ICalApi;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/***
 * Handles the connection to the rest server to receive ICals.
 * Stores the results and fires events on success
 */
public class ICalModel {
    private static final int ELEMENT_COUNT = 10;

    private ObservableArrayList<ICal> mICals;
    private ICalApi mICalApi;
    private boolean mICalsRequested;
    private RuntimeExceptionDao<ICal, Long> mICalDao;
    private long mTimeStampLastUpdate;
    private Date mDateLastDisplayedICals;
    private boolean mEndReached;

    private Callback<List<ICal>> mICalApiCallback = new Callback<List<ICal>>() {
        @Override
        public void success(List<ICal> iCals, Response response) {
            mICals.addAll(iCals);
            mICalsRequested = false;

            for(ICal i : iCals)
            {
                createIfNotExists(mICalDao, i);
            }
            mDateLastDisplayedICals = mICals.get(mICals.size()-1).getStartAsDate();
        }

        @Override
        public void failure(RetrofitError error) {
            mICalsRequested = false;
            if(error.getMessage().equals("404 Not Found"))
            {
                mEndReached = true;
            }
        }
    };

    private Callback<List<ICal>> mICalApiCallbackUpdate = new Callback<List<ICal>>() {
        @Override
        public void success(List<ICal> iCals, Response response) {
            mICalsRequested = false;

            for(ICal i : iCals)
            {
                createIfNotExists(mICalDao, i);
            }

            mICals.clear();
            mDateLastDisplayedICals = new Date(System.currentTimeMillis());
            mEndReached = false;
            fetchNextICals();
            mTimeStampLastUpdate = System.currentTimeMillis();
        }

        @Override
        public void failure(RetrofitError error) {
            mICalsRequested = false;
        }
    };

    @Inject
    public ICalModel(ICalApi api, RuntimeExceptionDao<ICal, Long> iCalDao){
        mICalApi = api;
        mICalDao = iCalDao;
        mICals = new ObservableArrayList<>();
        mICalsRequested = false;
        mTimeStampLastUpdate = 0;
        mEndReached = false;
        mDateLastDisplayedICals = new Date(System.currentTimeMillis());
        fetchNextICals();
    }

    public void fetchNextICals() {
        //check whether to get news from database or server
        if (!mICalsRequested)// && mICals.size() + ELEMENT_COUNT > mICalDao.countOf() && !mEndReached)
        {
            mICalsRequested = true;
            mICalApi.find(mICals.size(), ELEMENT_COUNT, mICalApiCallback);
        }
        /*
        else if (!mICalsRequested && !mEndReached)
        {
            mICalsRequested = true;
            List<ICal> fetchedICals = new ArrayList<>();
            //get next Element_count elements from database
            QueryBuilder<ICal, Long> queryBuilder = mICalDao.queryBuilder();
            //sort elements in ascending order according to startdate and only return
            //ELEMENT_COUNT iCals
            queryBuilder.orderBy("start", true).limit(ELEMENT_COUNT);
            try {
                queryBuilder.where().gt("start", mDateLastDisplayedICals.toString());
                fetchedICals = mICalDao.query(queryBuilder.prepare());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            mICals.addAll(fetchedICals);

            mDateLastDisplayedICals = mICals.get(mICals.size()-1).getStartAsDate();
            mICalsRequested = false;
        }
        */
    }


    public ObservableArrayList<ICal> getICalsList() {
        return mICals;
    }

    public void iCalModelUpdate()
    {
        //mICalApi.f
    }

    private void createIfNotExists(RuntimeExceptionDao<ICal, Long> iCalDao, ICal iCal)
    {
        List<ICal> retrievedICals = new ArrayList<>();
        QueryBuilder<ICal, Long> queryBuilder = iCalDao.queryBuilder();
        try {
            queryBuilder.where().eq("summery", iCal.getSummery()).and().eq("start", iCal.getStart())
            .and().eq("end", iCal.getEnd());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(retrievedICals.size() == 0)
        {
            iCalDao.create(iCal);
        }
    }
}
