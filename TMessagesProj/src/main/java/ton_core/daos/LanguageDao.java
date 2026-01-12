package ton_core.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import ton_core.entities.LanguageEntity;

@Dao
public interface LanguageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<LanguageEntity> languages);

    @Query("SELECT * FROM language")
    LiveData<List<LanguageEntity>> getLanguages();
}
