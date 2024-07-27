package sg.edu.np.mad.mad_p01_team4;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

public class SpeechRecognitionHelper {
    private static final String TAG = "SpeechRecognitionHelper";
    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;
    private SpeechRecognitionListener listener;

    public SpeechRecognitionHelper(Context context, SpeechRecognitionListener listener) {
        this.listener = listener;
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                listener.onReadyForSpeech();
                Log.d(TAG, "onReadyForSpeech");
            }

            @Override
            public void onBeginningOfSpeech() {
                listener.onBeginningOfSpeech();
                Log.d(TAG, "onBeginningOfSpeech");
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                listener.onRmsChanged(rmsdB);
                Log.d(TAG, "onRmsChanged: " + rmsdB);
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                listener.onBufferReceived(buffer);
                Log.d(TAG, "onBufferReceived");
            }

            @Override
            public void onEndOfSpeech() {
                listener.onEndOfSpeech();
                Log.d(TAG, "onEndOfSpeech");
            }

            @Override
            public void onError(int error) {
                listener.onError(error);
                String errorMessage = getErrorText(error);
                Log.e(TAG, "Speech recognition error: " + error);
                Log.e(TAG, "Error message: " + errorMessage);

                // Optionally, provide feedback to the user or retry
                if (error == SpeechRecognizer.ERROR_NO_MATCH) {
                    listener.onResults("No match found, please try again.");
                }
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && matches.size() > 0) {
                    Log.d(TAG, "Recognized results: " + matches.get(0));
                    listener.onResults(matches.get(0));
                } else {
                    listener.onResults("No results, please try again.");
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && matches.size() > 0) {
                    Log.d(TAG, "Partial results: " + matches.get(0));
                    listener.onPartialResults(matches.get(0));
                }
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                listener.onEvent(eventType, params);
                Log.d(TAG, "onEvent: " + eventType);
            }
        });

        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true); // Enable partial results
    }

    public void startListening() {
        Log.d(TAG, "Starting to listen");
        speechRecognizer.startListening(recognizerIntent);
    }

    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

    public interface SpeechRecognitionListener {
        void onReadyForSpeech();
        void onBeginningOfSpeech();
        void onRmsChanged(float rmsdB);
        void onBufferReceived(byte[] buffer);
        void onEndOfSpeech();
        void onError(int error);
        void onResults(String result);
        void onPartialResults(String partialResult);
        void onEvent(int eventType, Bundle params);
    }

    private String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error, please try again.";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client error, please try again.";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions, please enable microphone access.";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error, please check your connection and try again.";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout, please try again.";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match found, please try again.";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "Recognition service is busy, please try again.";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "Server error, please try again.";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input, please try again.";
                break;
            default:
                message = "Unknown error, please try again.";
                break;
        }
        return message;
    }
}
