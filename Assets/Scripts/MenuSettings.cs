using UnityEngine;

/// <summary>
/// Reads the settings saved by MenuOverlay.java. Call from the game's Unity code
/// and connect each value to your own game systems.
/// </summary>
public static class MenuSettings
{
    public static bool GetBool(string key, bool fallback = false)
    {
#if UNITY_ANDROID && !UNITY_EDITOR
        try
        {
            using (var unityPlayer = new AndroidJavaClass("com.unity3d.player.UnityPlayer"))
            using (var activity = unityPlayer.GetStatic<AndroidJavaObject>("currentActivity"))
            using (var prefs = activity.Call<AndroidJavaObject>("getSharedPreferences", "cf_menu_settings", 0))
            {
                return prefs.Call<bool>("getBoolean", key, fallback);
            }
        }
        catch (System.Exception e)
        {
            Debug.LogWarning("MenuSettings: " + e.Message);
        }
#endif
        return fallback;
    }

    public static string GetChoice(string key, string fallback = "")
    {
#if UNITY_ANDROID && !UNITY_EDITOR
        try
        {
            using (var unityPlayer = new AndroidJavaClass("com.unity3d.player.UnityPlayer"))
            using (var activity = unityPlayer.GetStatic<AndroidJavaObject>("currentActivity"))
            using (var prefs = activity.Call<AndroidJavaObject>("getSharedPreferences", "cf_menu_settings", 0))
            {
                return prefs.Call<string>("getString", "choice_" + key, fallback);
            }
        }
        catch (System.Exception e)
        {
            Debug.LogWarning("MenuSettings: " + e.Message);
        }
#endif
        return fallback;
    }
}
