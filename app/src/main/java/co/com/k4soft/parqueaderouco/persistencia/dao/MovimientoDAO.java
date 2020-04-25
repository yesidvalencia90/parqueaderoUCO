package co.com.k4soft.parqueaderouco.persistencia.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import co.com.k4soft.parqueaderouco.entidades.Movimiento;

@Dao
public interface MovimientoDAO {

    @Query("SELECT * FROM MOVIMIENTO Where placa=:placa AND finalizaMovimiento = 0")
    Movimiento  findByPLaca(String placa);

    @Insert
    void insert(Movimiento movimiento);
}
