package com.br.gerenciadordetreino.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.br.gerenciadordetreino.R;
import com.br.gerenciadordetreino.firebase.UserFirebase;
import com.br.gerenciadordetreino.model.User;
import com.br.gerenciadordetreino.model.enums.UsuarioEnum;
import com.br.gerenciadordetreino.persistence.UserDAO;
import com.br.gerenciadordetreino.utils.Mask;
import com.br.gerenciadordetreino.utils.PhotoUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.io.FileNotFoundException;
import java.io.IOException;

@EActivity(R.layout.activity_cadastro)
public class CadastroActivity extends SuperActivity {
    private static final int CODE_CAMERA = 30;
    public static final String FOTO_USER = "foto_user";

    @ViewById(R.id.edt_nome)
    EditText edtNome;
    @ViewById(R.id.edt_data_nascimento)
    EditText edtIdade;
    @ViewById(R.id.radio_sexo)
    RadioGroup radioSexo;
    @ViewById(R.id.img_circular_foto)
    ImageView imgUser;
    @ViewById(R.id.rb_femea)
    RadioButton rbFemea;
    @ViewById(R.id.rb_macho)
    RadioButton rbMacho;

    @Extra("isEdicao")
    boolean isEdicao;
    private User user;
    private String nomePath;
    private Bitmap bitmap;


    @AfterViews
    void init() {
        addMasks();
        user = UserDAO.getUser(CadastroActivity.this);
        if(user == null){
            user = new User();

        }else{
            if(isEdicao){
                setValuesUsuarioInFields();
            }else {
                goActivity();
            }
        }
    }

    @Click(R.id.btn_cadastrar)
    void cadastrar() {
        setValuesInUsuario();
        if(validadeFields()) {
            saveUserInFirebase();
            saveUserInDatabase();

            goActivity();
        }else{
            //TODO:"Preencha todos os campos" Dialog
        }

    }

    @Click(R.id.ll_editar_foto)
    void tirarFoto(){
        iniciarCamera();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CODE_CAMERA:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        bitmap = (Bitmap) data.getExtras().get("data");
                        nomePath = FOTO_USER;
                        imgUser.setImageBitmap(bitmap);
                    } else {
                        //errro
                    }
                } else {
                    //erro
                }
                break;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void iniciarCamera() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(i, CODE_CAMERA);
    }


    private void setValuesUsuarioInFields() {
        carregaFoto();
        edtNome.setText(user.getNome());
        edtIdade.setText(user.getDataNascimento());

        if(user.getSexo().equals(UsuarioEnum.MACHO.name())){
            rbMacho.setChecked(true);
            rbFemea.setChecked(false);
        }else{
            rbFemea.setChecked(true);
            rbMacho.setChecked(false);
        }
    }

    private void setValuesInUsuario() {
        int idEscolhaSexo = radioSexo.getCheckedRadioButtonId();
        user.setNome(edtNome.getText().toString());
        user.setDataNascimento(edtIdade.getText().toString());
        user.setPathFoto(nomePath);

        try {
            PhotoUtils.savePhotoInFile(this,bitmap, FOTO_USER );
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(idEscolhaSexo == R.id.rb_macho){
            user.setSexo(UsuarioEnum.MACHO.name());
        }else if(idEscolhaSexo == R.id.rb_femea){
            user.setSexo(UsuarioEnum.FÊMEA.name());
        }
    }

    private void carregaFoto() {
        try {
            bitmap = PhotoUtils.getImage(CadastroActivity.this, FOTO_USER);
            if(bitmap != null){
                imgUser.setImageBitmap(bitmap);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void saveUserInFirebase(){
        String android_id = Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
       user.setId(android_id);
        UserFirebase userFirebase = new UserFirebase();
        userFirebase.save(user);
    }
    private void saveUserInDatabase(){
        user.setId("1");
        UserDAO.createUser(this, user);
    }

    private boolean validadeFields(){
        //TODO:validar campos vazios
        return  true;
    }
    private void goActivity() {
        Intent intent = new Intent(CadastroActivity.this, HomeActivity_.class);
        intent.putExtra("Usuario", user);
        startActivity(intent);
        finish();
    }

    private void addMasks(){
        edtIdade.addTextChangedListener(Mask.insert("##/##/####",edtIdade));
    }


}
