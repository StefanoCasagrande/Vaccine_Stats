package it.stefanocasagrande.vaccini_stats.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

public class PageViewModel extends ViewModel {

    private MutableLiveData<Integer> mIndex = new MutableLiveData<>();
    private LiveData<String> mText = Transformations.map(mIndex, input -> {

        switch (input)
        {
            case 1:
                return "Totale";
            case 2:
                return "16-19";
            case 3:
                return "20-29";
            case 4:
                return "30-39";
            case 5:
                return "40-49";
            case 6:
                return "50-59";
            case 7:
                return "60-69";
            case 8:
                return "70-79";
            case 9:
                return "80-89";
            case 10:
                return "90+";
            default:
                return "";
        }
    });

    public void setIndex(int index) {
        mIndex.setValue(index);
    }

    public LiveData<String> getText() {
        return mText;
    }
}
