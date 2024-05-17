package com.example.kangarun;

import static org.mockito.Mockito.*;

import static org.junit.Assert.assertEquals;

import com.google.firebase.firestore.DocumentSnapshot;

import org.junit.Test;
import org.robolectric.RobolectricTestRunner;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import com.example.kangarun.activity.ExerciseRecordActivity;

import java.util.ArrayList;


@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class ExerciseRecordTest {
    private ExerciseRecordActivity testActivity;

    @Test
    public void implementSortListsTest() {
        testActivity = new ExerciseRecordActivity();
        DocumentSnapshot doc1 = mock(DocumentSnapshot.class);
        DocumentSnapshot doc2 = mock(DocumentSnapshot.class);
        DocumentSnapshot doc3 = mock(DocumentSnapshot.class);

        when(doc1.contains("uid")).thenReturn(true);
        when(doc1.getString("uid")).thenReturn("1");
        when(doc1.contains("date")).thenReturn(true);
        when(doc1.getString("date")).thenReturn("2024-05-10 15:59:02");
        when(doc1.contains("duration")).thenReturn(true);
        when(doc1.getString("duration")).thenReturn("00:43");
        when(doc1.contains("distance")).thenReturn(true);
        when(doc1.getDouble("distance")).thenReturn(5886.07080078125);

        when(doc2.contains("uid")).thenReturn(true);
        when(doc2.getString("uid")).thenReturn("2");
        when(doc2.contains("date")).thenReturn(true);
        when(doc2.getString("date")).thenReturn("2024-05-03 10:35:34");
        when(doc2.contains("duration")).thenReturn(true);
        when(doc2.getString("duration")).thenReturn("04:56");
        when(doc2.contains("distance")).thenReturn(true);
        when(doc2.getDouble("distance")).thenReturn(365.09036571905017);

        when(doc3.contains("uid")).thenReturn(true);
        when(doc3.getString("uid")).thenReturn("3");
        when(doc3.contains("date")).thenReturn(true);
        when(doc3.getString("date")).thenReturn("2024-05-02 07:19:09");
        when(doc3.contains("duration")).thenReturn(true);
        when(doc3.getString("duration")).thenReturn("00:40");
        when(doc3.contains("distance")).thenReturn(true);
        when(doc3.getDouble("distance")).thenReturn(1065.2303226590157);

        testActivity.list = new ArrayList<>();
        testActivity.list.add(doc1);
        testActivity.list.add(doc2);
        testActivity.list.add(doc3);
        testActivity.implementSortLists();

        assertEquals("1", testActivity.dateDeslist.get(0).getString("uid"), "1");
        assertEquals("2", testActivity.dateDeslist.get(1).getString("uid"), "2");
        assertEquals("3", testActivity.dateDeslist.get(2).getString("uid"), "3");

        assertEquals("4", testActivity.dateAsclist.get(0).getString("uid"), "3");
        assertEquals("5", testActivity.dateAsclist.get(1).getString("uid"), "2");
        assertEquals("6", testActivity.dateAsclist.get(2).getString("uid"), "1");

        assertEquals("7", testActivity.distanceDeslist.get(0).getString("uid"), "1");
        assertEquals("8", testActivity.distanceDeslist.get(1).getString("uid"), "3");
        assertEquals("9", testActivity.distanceDeslist.get(2).getString("uid"), "2");

        assertEquals("10", testActivity.distanceAsclist.get(0).getString("uid"), "2");
        assertEquals("11", testActivity.distanceAsclist.get(1).getString("uid"), "3");
        assertEquals("12", testActivity.distanceAsclist.get(2).getString("uid"), "1");

        assertEquals("13", testActivity.durationDeslist.get(0).getString("uid"), "2");
        assertEquals("14", testActivity.durationDeslist.get(1).getString("uid"), "1");
        assertEquals("15", testActivity.durationDeslist.get(2).getString("uid"), "3");

        assertEquals("16", testActivity.durationAsclist.get(0).getString("uid"), "3");
        assertEquals("17", testActivity.durationAsclist.get(1).getString("uid"), "1");
        assertEquals("18", testActivity.durationAsclist.get(2).getString("uid"), "2");
    }

}
