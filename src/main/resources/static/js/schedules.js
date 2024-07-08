document.addEventListener("DOMContentLoaded", function() {
    const currentDate = new Date();
    let currentMonth = currentDate.getMonth();
    let currentYear = currentDate.getFullYear();

    const monthNames = [
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    ];

    const monthElement = document.getElementById("currentMonth");

    function updateCalendar() {
        // Clear previous calendar
        const calendarGrid = document.querySelector(".calendar-grid");
        calendarGrid.innerHTML = "";

        // Display month and year
        monthElement.textContent = `${monthNames[currentMonth]} ${currentYear}`;

        // Calculate the first day of the month
        const firstDayOfMonth = new Date(currentYear, currentMonth, 1).getDay();
        const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();

        // Generate days of the week headers
        const daysOfWeek = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
        daysOfWeek.forEach(day => {
            const dayName = document.createElement("div");
            dayName.classList.add("day-name");
            dayName.textContent = day;
            calendarGrid.appendChild(dayName);
        });

        // Generate days of the month
        let dayIndex = 0;
        for (let i = 0; i < 6; i++) { // Assume maximum 6 weeks for display
            for (let j = 0; j < 7; j++) {
                const calendarSquare = document.createElement("div");
                calendarSquare.classList.add("calendar-square");

                if (i === 0 && j < firstDayOfMonth) {
                    // Empty square before the first day of the month
                    calendarSquare.textContent = "";
                } else {
                    dayIndex++;
                    if (dayIndex > daysInMonth) {
                        // No more days in the month, leave square empty
                        calendarSquare.textContent = "";
                    } else {
                        calendarSquare.textContent = dayIndex;

                        // Fetch events from Google Calendar API for this day
                        fetchEventsFromGoogleCalendar(currentYear, currentMonth, dayIndex, calendarSquare);
                    }
                }

                calendarGrid.appendChild(calendarSquare);
            }
        }
    }

    function fetchEventsFromGoogleCalendar(year, month, day, calendarSquare) {
        // Replace with actual API call to fetch events from Google Calendar
        // Example using placeholder for demonstration
        const url = '/fetch-events?year=' + year + '&month=' + (month + 1) + '&day=' + day;

        fetch(url)
            .then(response => response.json())
            .then(data => {
                // Process the fetched events and display them in calendarSquare
                if (data && data.events) {
                    data.events.forEach(event => {
                        const eventElement = document.createElement("div");
                        eventElement.classList.add("event");
                        eventElement.textContent = event.summary;
                        calendarSquare.appendChild(eventElement);
                    });
                }
            })
            .catch(error => {
                console.error('Error fetching events:', error);
            });
    }

    updateCalendar();

    // Handle month navigation
    document.getElementById("prevMonth").addEventListener("click", function() {
        if (currentMonth > 0) {
            currentMonth--;
        } else {
            currentMonth = 11;
            currentYear--;
        }
        updateCalendar();
    });

    document.getElementById("nextMonth").addEventListener("click", function() {
        if (currentMonth < 11) {
            currentMonth++;
        } else {
            currentMonth = 0;
            currentYear++;
        }
        updateCalendar();
    });
});
