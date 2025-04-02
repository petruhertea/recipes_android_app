package com.cookcraft.mvvm;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.cookcraft.models.AvailableIngredient;
import com.cookcraft.room.IngredientDAO;
import com.cookcraft.room.IngredientDatabase;

import java.util.List;

public class IngredientRepository {
    private IngredientDAO ingredientDAO;
    private LiveData<List<AvailableIngredient>> availableIngredientList;

    public IngredientRepository(Application application) {
        IngredientDatabase ingredientDatabase = IngredientDatabase.getInstance(application);
        ingredientDAO = ingredientDatabase.ingredientDAO();
        availableIngredientList = ingredientDAO.getAllIngredients();
    }

    public void insertData(AvailableIngredient availableIngredient) {
        new InsertAsyncTask(ingredientDAO).execute(availableIngredient);
    }

    public void deleteData(AvailableIngredient availableIngredient) {
        new DeleteAsyncTask(ingredientDAO).execute(availableIngredient);
    }

    public void updateData(AvailableIngredient availableIngredient) {
        new UpdateAsyncTask(ingredientDAO).execute(availableIngredient);
    }

    public LiveData<List<AvailableIngredient>> getAllData() {
        return availableIngredientList;
    }

    public void deleteAllData() {
        new DeleteAllAsyncTask(ingredientDAO).execute();
    }


    public static class InsertAsyncTask extends AsyncTask<AvailableIngredient, Void, Void> {

        private IngredientDAO ingredientDAO;

        public InsertAsyncTask(IngredientDAO ingredientDAO) {
            this.ingredientDAO = ingredientDAO;
        }

        @Override
        protected Void doInBackground(AvailableIngredient... availableIngredients) {
            ingredientDAO.insertIngredient(availableIngredients[0]);
            return null;
        }
    }

    public static class UpdateAsyncTask extends AsyncTask<AvailableIngredient, Void, Void> {

        private IngredientDAO ingredientDAO;

        public UpdateAsyncTask(IngredientDAO ingredientDAO) {
            this.ingredientDAO = ingredientDAO;
        }

        @Override
        protected Void doInBackground(AvailableIngredient... availableIngredients) {
            ingredientDAO.updateIngredient(availableIngredients[0]);
            return null;
        }
    }

    public static class DeleteAsyncTask extends AsyncTask<AvailableIngredient, Void, Void> {

        private IngredientDAO ingredientDAO;

        public DeleteAsyncTask(IngredientDAO ingredientDAO) {
            this.ingredientDAO = ingredientDAO;
        }

        @Override
        protected Void doInBackground(AvailableIngredient... availableIngredients) {
            ingredientDAO.deleteIngredient(availableIngredients[0]);
            return null;
        }
    }

    public static class DeleteAllAsyncTask extends AsyncTask<Void, Void, Void> {

        private IngredientDAO ingredientDAO;

        public DeleteAllAsyncTask(IngredientDAO ingredientDAO) {
            this.ingredientDAO = ingredientDAO;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ingredientDAO.deleteAll();
            return null;
        }
    }
}
