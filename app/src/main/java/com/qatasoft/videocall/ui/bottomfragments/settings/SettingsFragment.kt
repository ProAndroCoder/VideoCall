package com.qatasoft.videocall.ui.bottomfragments.settings


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.qatasoft.videocall.R

/**
 * A simple [Fragment] subclass.
 */
class SettingsFragment : Fragment() {

    private val manager = activity?.supportFragmentManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Çıkış Yapma Fonksiyonu
        signOut()


    }

    private fun signOut() {
//        activity!!.stopService(Intent(context, BackgroundService::class.java))
//        //Shared Preference ile telefonda bulunan kullanıcı bilgilerini silme işlemi. Uid boş gönderilirse çıkış yapar.
//        val myPreference = MyPreference(context)
//
//        myPreference.setLoginInfo(LoginInfo("", ""))
//        myPreference.setUserInfo(User("", "", "", "","","","",false))
//
//        // Firebase ile kullanıcının çıkışını sağlamak ve onu LoginActivity'e yollama işi
//        FirebaseAuth.getInstance().signOut()
//        val intent = Intent(context, LoginActivity::class.java)
//        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
//        startActivity(intent)
    }
}