# MemoryGameProject

## Overview
This is a team CA Project undertaken at NUS ISS Systems Analysis graduate diploma course. The objective is to create a memory game using Android(Java).

## Requirements
1. Only two Activities are required for the app.
2. The first Activity allows a URL to be specified. Clicking on the Fetch button will extract the first 20 images that it finds on the webpage that the URL points to and display the downloaded images in a grid. A progress-bar should show the number of images downloaded so far with description text (e.g. Downloading 10 of 20 images …)
3. The user can change the URL in the middle of a download and click on the Fetch button again. The current download would then be aborted and all images in the grid will be cleared. Repeat step 2 based on the new URL specified. 

![image](https://user-images.githubusercontent.com/78467063/125014682-b9b57c00-e0a0-11eb-96e5-3fc3f4fd38f8.png)

4. Recommended to use https://stocksnap.io as your URL source as the image extraction at that site is straightforward.
5. Once the first 20 images have been downloaded, allow the user to select 6 of them.
6. Once 6 images have been selected, bring the user to the second Activity.
7. The second Activity should look like this:

![image](https://user-images.githubusercontent.com/78467063/125014750-d651b400-e0a0-11eb-829e-562477dcca28.png)

8. The second Activity will take the 6 selected images and use them for the memory game. In the beginning, display 12 placeholders. Then when a placeholder is touched, reveals the image behind that placeholder and wait for the second placeholder to be touched. When the user touched the second placeholder, reveals the image behind that placeholder. If both images are identical, leave both images as they are. If they are different, hide the two images and revert back to display the two placeholders.3 of 6 matches
9. The top left corner should display the matches so far and the top right corner should show a run-up timer.
10. When all images matches, return to the first Activity automatically. User can enter a different URL and download new images to play the game again.
